package com.danuvius.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.danuvius.platform.mapper.*;
import com.danuvius.platform.model.dto.metric.MetricReportItemDTO;
import com.danuvius.platform.model.entity.*;
import com.danuvius.platform.model.enums.*;
import com.danuvius.platform.service.MetricLogService;
import com.danuvius.platform.util.CacheKeyUtil;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class MetricLogServiceImpl extends ServiceImpl<MetricLogMapper, MetricLog>
        implements MetricLogService {

    private static final String ROLE_MASTER = "MASTER";
    @Value("${platform.retain-days:7}")
    private int retainDays;

    private final MetricLogMapper metricLogMapper;
    private final NodeStatusMapper nodeStatusMapper;
    private final RedisInstanceMapper redisInstanceMapper;
    private final RedisSwitchEventMapper switchEventMapper;
    private final ExecutorInfoMapper executorInfoMapper;
    private final AlarmRecordMapper alarmRecordMapper;
    private final Cache<String, Object> caffeineCache;

    public MetricLogServiceImpl(MetricLogMapper metricLogMapper,
                                NodeStatusMapper nodeStatusMapper,
                                RedisInstanceMapper redisInstanceMapper,
                                RedisSwitchEventMapper switchEventMapper,
                                ExecutorInfoMapper executorInfoMapper,
                                AlarmRecordMapper alarmRecordMapper,
                                Cache<String, Object> caffeineCache) {
        this.metricLogMapper = metricLogMapper;
        this.nodeStatusMapper = nodeStatusMapper;
        this.redisInstanceMapper = redisInstanceMapper;
        this.switchEventMapper = switchEventMapper;
        this.executorInfoMapper = executorInfoMapper;
        this.alarmRecordMapper = alarmRecordMapper;
        this.caffeineCache = caffeineCache;
    }

    @Override
    public void reportMetric(MetricReportItemDTO metric) {
        ExecutorInfo executor = executorInfoMapper.selectByToken(metric.getToken());
        if (executor == null) {
            throw new RuntimeException("无效的token");
        }

        String instanceId = executor.getInstanceId();
        if (instanceId == null || instanceId.trim().isEmpty()) {
            throw new RuntimeException("执行器未绑定实例");
        }

        // 平台内部回填
        metric.setInstanceId(instanceId);

        LocalDateTime now = LocalDateTime.now();

        // 写指标日志
        MetricLog metricLog = new MetricLog();

        metricLog.setInstanceId(metric.getInstanceId());
        metricLog.setTm(now);
        metricLog.setCpuPer(metric.getCpuPer());
        metricLog.setMemServerPer(metric.getMemServerPer());
        metricLog.setMemUs(metric.getMemUs());
        metricLog.setLatency(metric.getLatency());
        metricLog.setIoIik(metric.getIoIik());
        metricLog.setIoIok(metric.getIoIok());
        metricLog.setOps(metric.getOps());
        metricLog.setRole(metric.getRole());
        metricLog.setRedisVersion(metric.getRedisVersion());
        metricLog.setRunId(metric.getRunId());
        metricLog.setMasterHost(metric.getMasterHost());
        metricLog.setMasterPort(metric.getMasterPort());
        metricLog.setReplDelay(metric.getReplDelay());

        metricLog.setCollectType("1".equals(metric.getCollectType()) ? "1" : "0");

        metricLog.setCreatedTime(now);
        metricLog.setUpdatedTime(now);
        metricLogMapper.insert(metricLog);

        // 采集上报成功时同步更新redis_instance 最后心跳时间与运行状态
        redisInstanceMapper.updateHeartbeatAndStatus(metric.getInstanceId(), now,
                StatusEnum.RUN_NORMAL.getCode());

        // 写节点状态日志
        NodeStatus ns = new NodeStatus();
        ns.setInstanceId(metric.getInstanceId());
        ns.setRunId(metric.getRunId());
        ns.setTm(now);
        // 能收到采集信息，默认在线
        ns.setCollectStatus("1");
        // 执行器端上报的连通性延迟，没有则默认 0
        ns.setPingLatency(metric.getPingLatency() != null ? metric.getPingLatency() : "0");
        ns.setRole(metric.getRole());
        ns.setErrorMsg(metric.getErrorMsg());
        ns.setCreatedTime(now);
        ns.setUpdatedTime(now);
        nodeStatusMapper.insert(ns);

        // 主从切换检测
        try {
            detectMasterSwitch(metric, now);
        } catch (Exception e) {
            log.error("[主从切换检测] 异常, instanceId={}", metric.getInstanceId(), e);
        }
    }

    private void detectMasterSwitch(MetricReportItemDTO metric, LocalDateTime now) {
        String instanceId = metric.getInstanceId();
        String role = metric.getRole();
        if (instanceId == null || role == null) {
            return;
        }

        RedisInstance inst = redisInstanceMapper.selectByInstanceId(instanceId);
        if (inst == null) {
            return;
        }

        boolean changed = false;
        if (!role.equalsIgnoreCase(inst.getRole())) {
            inst.setRole(role);
            changed = true;
        }
        if (ROLE_MASTER.equalsIgnoreCase(role)) {
            if (inst.getMasterHostIp() != null || inst.getMasterRedisPort() != null) {
                inst.setMasterHostIp(null);
                inst.setMasterRedisPort(null);
                changed = true;
            }
        } else {
            String masterHost = metric.getMasterHost();
            String masterPort = metric.getMasterPort();
            if (!equalsNullable(masterHost, inst.getMasterHostIp())
                    || !equalsNullable(masterPort, inst.getMasterRedisPort())) {
                inst.setMasterHostIp(masterHost);
                inst.setMasterRedisPort(masterPort);
                changed = true;
            }
        }
        if (changed) {
            inst.setUpdatedTime(now);
            redisInstanceMapper.updateById(inst);
        }

        if (!ROLE_MASTER.equalsIgnoreCase(role)) {
            return;
        }

        RedisSwitchEvent lastEvent = switchEventMapper.selectLatestSwitchEvent();
        String lastMasterId = lastEvent == null ? null : lastEvent.getNewMasterId();

        if (lastEvent == null) {
            insertSwitchEvent(null, instanceId, TypeEnum.INIT, now);
            log.info("[主从切换检测] 建立初始主基线: masterId={}", instanceId);
            return;
        }
        if (instanceId.equals(lastMasterId)) {
            return;
        }

        insertSwitchEvent(lastMasterId, instanceId, TypeEnum.AUTO, now);
        caffeineCache.invalidate(CacheKeyUtil.TOPOLOGY);
        caffeineCache.invalidate(CacheKeyUtil.NODES);
        log.info("[主从切换检测] 检测到主节点变更: {} -> {}", lastMasterId, instanceId);
    }

    private void insertSwitchEvent(String oldMasterId, String newMasterId,
                                   TypeEnum type, LocalDateTime now) {
        RedisSwitchEvent event = new RedisSwitchEvent();

        event.setOldMasterId(oldMasterId);
        event.setNewMasterId(newMasterId);
        event.setSwitchTime(now);
        event.setSwitchType(type.getCode());
        event.setSwitchResult(StatusEnum.SUCCESS.getCode());
        event.setCreatedTime(now);
        event.setUpdatedTime(now);
        switchEventMapper.insert(event);

        /* 检测到主节点变更时，除失效拓扑/节点状态缓存外，同时生成告警记录 */
        if (type == TypeEnum.AUTO) {
            generateSwitchAlarm(oldMasterId, newMasterId, now);
        }
    }

    /**
     * 主从切换生成告警记录
     * @param oldMasterId
     * @param newMasterId
     * @param now
     */
    private void generateSwitchAlarm(String oldMasterId, String newMasterId, LocalDateTime now) {
        try {
            AlarmRecord record = new AlarmRecord();
            record.setAlarmNo("AL" + System.currentTimeMillis()
                    + String.format("%03d", (int) (Math.random() * 1000)));
            // 系统切换事件，不关联具体规则
            record.setRuleId(null);
            record.setInstanceId(newMasterId);
            record.setMetricKey(TypeEnum.MASTER_SWITCH.getCode());
            record.setMetricName(TypeEnum.MASTER_SWITCH.getDesc());
            record.setAlarmLevel(AlarmLevelEnum.WARNING.getCode());
            record.setAlarmContent("检测到主从切换：原主节点 " + (oldMasterId == null ? "null" : oldMasterId)
                    + " → 新主节点 " + newMasterId);
            record.setTriggerValue("");
            record.setUnit("");
            record.setTriggerTime(now);
            record.setRecoverStatus(StatusEnum.UNRECOVER.getCode());
            record.setCreatedTime(now);
            record.setUpdatedTime(now);
            alarmRecordMapper.insert(record);
            log.info("[主从切换检测] 已生成切换告警记录, alarmNo={}", record.getAlarmNo());
        } catch (Exception e) {
            log.error("[主从切换检测] 生成切换告警记录失败, newMasterId={}", newMasterId, e);
        }
    }

    private boolean equalsNullable(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

    /**
     * 按实例取最新一条指标，拓扑节点状态判断用
     * @param instanceId
     * @return
     */
    @Override
    public MetricLog getLatestByInstanceId(String instanceId) {
        return metricLogMapper.selectLatestByInstanceId(instanceId);
    }

    @Override
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredData() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(retainDays);
        metricLogMapper.deleteExpiredBefore(threshold);
        nodeStatusMapper.deleteExpiredBefore(threshold);
    }
}