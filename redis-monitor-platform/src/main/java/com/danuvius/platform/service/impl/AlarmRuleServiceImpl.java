package com.danuvius.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.danuvius.platform.common.exception.BusinessCode;
import com.danuvius.platform.common.exception.BusinessException;
import com.danuvius.platform.mapper.*;
import com.danuvius.platform.model.dto.alarm.rule.*;
import com.danuvius.platform.model.dto.metric.MetricReportItemDTO;
import com.danuvius.platform.model.entity.AlarmRecord;
import com.danuvius.platform.model.entity.AlarmRule;
import com.danuvius.platform.model.entity.ExecutorInfo;
import com.danuvius.platform.model.entity.RedisInstance;
import com.danuvius.platform.model.enums.MetricTypeEnum;
import com.danuvius.platform.model.enums.StatusEnum;
import com.danuvius.platform.service.*;
import com.danuvius.platform.util.CompareUtil;
import com.danuvius.platform.util.DetectTaskRecorder;
import com.danuvius.platform.util.ParseUtils;
import com.danuvius.platform.util.RuleMatchHelper;
import com.danuvius.platform.util.http.AgentCollectResponse;
import com.danuvius.platform.util.http.AgentHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class AlarmRuleServiceImpl extends ServiceImpl<AlarmRuleMapper, AlarmRule>
        implements AlarmRuleService {
    private final AlarmRuleMapper ruleMapper;
    private final AlarmRecordMapper alarmRecordMapper;
    private final RedisInstanceMapper redisInstanceMapper;
    private final ExecutorInfoMapper executorInfoMapper;
    private final MetricLogMapper metricLogMapper;
    private final MetricLogService metricLogService;
    private final RuleMatchHelper ruleMatchHelper;
    private final AgentHttpClient agentHttpClient;
    /*告警邮件通知*/
    private final AlarmNoticeService alarmNoticeService;
    /*操作审计日志*/
    private final AuditLogService auditLogService;
    /*告警检测任务状态记录*/
    private final DetectTaskRecorder detectTaskRecorder;
    /*通用异步任务执行器（独立Bean保证@Async生效）*/
    private final AsyncTaskRunner asyncTaskRunner;

    /** 持续时长默认值，周期 */
    private static final int DEFAULT_DURATION_MIN = 1;
    /** 重复告警抑制默认值，s */
    private static final int DEFAULT_SILENCE_MIN = 1800;


    private static final String REPL_DELAY_METRIC_KEY = "replDelay";

    /*手动检测统一为异步提交，固定返回文案*/
    private static final String RESULT_HAS_ALARM_CHECK =
            "已触发手动检测，请稍后在告警记录中查看结果";
    private static final String RESULT_HAS_ALARM_ALL =
            "已触发全量检测，请稍后在告警记录中查看结果";

    // 自动恢复：指标连续 N 个采集周期回落阈值以下自动恢复，默认 2；<=0 关闭
    @Value("${platform.alarm.auto-recover.periods:2}")
    private int autoRecoverPeriods;

    public AlarmRuleServiceImpl(AlarmRuleMapper ruleMapper,
                                AlarmRecordMapper alarmRecordMapper,
                                RedisInstanceMapper redisInstanceMapper,
                                ExecutorInfoMapper executorInfoMapper,
                                MetricLogMapper metricLogMapper,
                                MetricLogService metricLogService,
                                RuleMatchHelper ruleMatchHelper,
                                AgentHttpClient agentHttpClient,
                                AlarmNoticeService alarmNoticeService,
                                AuditLogService auditLogService,
                                DetectTaskRecorder detectTaskRecorder,
                                AsyncTaskRunner asyncTaskRunner) {
        this.ruleMapper = ruleMapper;
        this.alarmRecordMapper = alarmRecordMapper;
        this.redisInstanceMapper = redisInstanceMapper;
        this.executorInfoMapper = executorInfoMapper;
        this.metricLogMapper = metricLogMapper;
        this.metricLogService = metricLogService;
        this.ruleMatchHelper = ruleMatchHelper;
        this.agentHttpClient = agentHttpClient;
        this.alarmNoticeService = alarmNoticeService;
        this.auditLogService = auditLogService;
        this.detectTaskRecorder = detectTaskRecorder;
        this.asyncTaskRunner = asyncTaskRunner;
    }

    /**
     * 规则列表分页
     * @param req
     * @return
     */
    @Override
    public RulePageResponse getRulePage(RuleListRequest req) {
        int pageNum = req.getPageNum() == null || req.getPageNum() < 1 ? 1 : req.getPageNum();
        int pageSize = req.getPageSize() == null || req.getPageSize() < 1 ? 12 : req.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        String keyword = req.getKeyword();
        String alarmLevel=req.getAlarmLevel();
        List<String> scopeInstanceIds=req.getScopeInstanceIds();
        String enableStatus=req.getEnableStatus();


        //mapper查规则，返回全记录
        List<RuleItemDTO> list = ruleMapper.selectRulePage(keyword, alarmLevel, scopeInstanceIds, enableStatus,offset, pageSize);
        long total = ruleMapper.countRulePage(keyword, alarmLevel, scopeInstanceIds, enableStatus);

        RulePageResponse resp = new RulePageResponse();
        resp.setTotal(total);
        resp.setPageNum(pageNum);
        resp.setPageSize(pageSize);
        resp.setTotalPages((int) Math.ceil((double) total / pageSize));
        /*回显生效实例名称展示数组 scopeInstanceNames */
        fillScopeInstanceNames(list);
        resp.setList(list);
        return resp;
    }

    /**
     *将规则的生效实例id转换为实例名称数组。
     */
    private void fillScopeInstanceNames(List<RuleItemDTO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<String> allIds = new LinkedHashSet<>();
        for (RuleItemDTO item : list) {
            if (item.getScopeInstanceId() == null || item.getScopeInstanceId().trim().isEmpty()) {
                continue;
            }
            for (String id : item.getScopeInstanceId().split(",")) {
                String trimmed = id.trim();
                if (!trimmed.isEmpty()) {
                    allIds.add(trimmed);
                }
            }
        }
        if (allIds.isEmpty()) {
            return;
        }
        List<RedisInstance> instances = redisInstanceMapper.selectInstancesByIds(new ArrayList<>(allIds));
        Map<String, String> idNameMap = new HashMap<>();
        if (instances != null) {
            for (RedisInstance instance : instances) {
                idNameMap.put(instance.getInstanceId(), instance.getInstanceName());
            }
        }
        for (RuleItemDTO item : list) {
            if (item.getScopeInstanceId() == null || item.getScopeInstanceId().trim().isEmpty()) {
                continue;
            }
            List<String> names = new ArrayList<>();
            for (String id : item.getScopeInstanceId().split(",")) {
                String trimmed = id.trim();
                if (!trimmed.isEmpty()) {
                    names.add(idNameMap.getOrDefault(trimmed, trimmed));
                }
            }
            item.setScopeInstanceNames(names);
        }
    }

    /**
     * 加规则
     * @param req
     * @return 新增规则id
     */
    @Override
    public String addRule(RuleAddRequest req) {
        /*同指标同生效节点下已存在启用规则时，提示「该节点已存在相同指标规则」 */
        checkDuplicateRule(req.getMetricKey(), req.getScopeType(), req.getScopeInstanceIds(), null);

        AlarmRule rule = new AlarmRule();
        rule.setRuleId("RL_" + System.currentTimeMillis());
        rule.setRuleName(req.getRuleName());
        rule.setMetricKey(req.getMetricKey());
        MetricTypeEnum metricType = MetricTypeEnum.of(req.getMetricKey());
        if (metricType != null) {
            rule.setMetricName(metricType.getName());
            if (req.getUnit() == null) {
                rule.setUnit(metricType.getUnit());
            }
        }
        rule.setCompareType(req.getCompareType());

        //Double转String
        rule.setThreshold(ParseUtils.doubleToString(req.getThreshold()));

        rule.setUnit(ParseUtils.defaultIfBlank(req.getUnit(), metricType == null ? null : metricType.getUnit()));

        rule.setCollectInterval(ParseUtils.defaultIfBlank(req.getCollectInterval(), "300"));
        rule.setDuration(ParseUtils.defaultIfBlank(req.getDuration(), "1"));
        rule.setAlarmLevel(ParseUtils.defaultIfBlank(req.getAlarmLevel(), "2"));
        rule.setSilenceDuration(ParseUtils.defaultIfBlank(req.getSilenceDuration(), "1800"));
        rule.setScopeType(ParseUtils.defaultIfBlank(req.getScopeType(), "1"));
        rule.setScopeInstanceId(ParseUtils.joinIds(req.getScopeInstanceIds()));
        //默认启用
        Boolean enableStatus = req.getEnableStatus();
        rule.setEnableStatus(enableStatus == null ? "1" : (enableStatus ? "1" : "0"));
        rule.setDelFlag("0");

        rule.setCreatedBy(req.getCreatedBy());
        rule.setCreatedTime(LocalDateTime.now());
        ruleMapper.insert(rule);
        /* 将操作人、规则id、规则名、操作类型、操作时间记入操作审计日志 */
        auditLogService.record("告警规则", "新增", rule.getRuleId(),
                "新增告警规则：" + req.getRuleName());
        return rule.getRuleId();
    }

    /**
     * 重复规则校验。
     * 同指标且生效节点存在交集（任一已启用规则作用于待新增规则的任一生效节点）时视为重复。
     *
     * @param metricKey        监控指标
     * @param scopeType        生效范围：1 全部节点 2 指定节点
     * @param scopeInstanceIds 生效节点列表
     * @param excludeRuleId    排除的规则id（修改场景排除自身）
     */
    private void checkDuplicateRule(String metricKey, String scopeType,
                                    List<String> scopeInstanceIds, String excludeRuleId) {
        List<AlarmRule> sameMetricRules = ruleMapper.selectEnabledRulesByMetric(metricKey);
        if (sameMetricRules == null || sameMetricRules.isEmpty()) {
            return;
        }
        // 新规则生效节点（scopeType=1 时表示全部节点）
        List<String> newScopeIds = new ArrayList<>();
        if ("1".equals(scopeType)) {
            List<RedisInstance> all = redisInstanceMapper.selectActiveInstances("1");
            if (all != null) {
                for (RedisInstance instance : all) {
                    newScopeIds.add(instance.getInstanceId());
                }
            }
        } else if (scopeInstanceIds != null) {
            for (String id : scopeInstanceIds) {
                if (id != null && !id.trim().isEmpty()) {
                    newScopeIds.add(id.trim());
                }
            }
        }
        for (AlarmRule exist : sameMetricRules) {
            if (excludeRuleId != null && excludeRuleId.equals(exist.getRuleId())) {
                continue;
            }
            for (String instanceId : newScopeIds) {
                if (ruleMatchHelper.isRuleApplyToInstance(exist, instanceId)) {
                    throw new BusinessException(BusinessCode.OPERATION_ERROR.getCode(), "该节点已存在相同指标规则");
                }
            }
        }
    }



    /**
     * 改规则
     * @param req
     * @return
     */
    @Override
    public boolean updateRule(UpdateRequest req) {
        AlarmRule rule = ruleMapper.selectByRuleId(req.getRuleId());

        if (rule == null) throw new BusinessException(BusinessCode.NOT_FOUND_ERROR.getCode(), "规则不存在");
        /* 修改时同样做重复规则校验（排除自身） */
        checkDuplicateRule(req.getMetricKey(), req.getScopeType(), req.getScopeInstanceIds(), rule.getRuleId());
        rule.setRuleName(req.getRuleName());
        rule.setMetricKey(req.getMetricKey());
        MetricTypeEnum metricType = MetricTypeEnum.of(req.getMetricKey());
        if (metricType != null) {
            rule.setMetricName(metricType.getName());
        }

        rule.setCompareType(req.getCompareType());

        rule.setThreshold(ParseUtils.doubleToString(req.getThreshold()));

        rule.setUnit(req.getUnit());
        /* 修改接口入参包含采集间隔 */
        rule.setCollectInterval(ParseUtils.defaultIfBlank(req.getCollectInterval(), "300"));
        rule.setDuration(req.getDuration());
        rule.setAlarmLevel(req.getAlarmLevel());
        rule.setSilenceDuration(req.getSilenceDuration());
        rule.setScopeType(req.getScopeType());

        rule.setScopeInstanceId(ParseUtils.joinIds(req.getScopeInstanceIds()));

        Boolean enable = req.getEnableStatus();
        if (enable != null) {
            rule.setEnableStatus(enable ? "1" : "0");
        }

        rule.setUpdatedTime(LocalDateTime.now());

        int rows=ruleMapper.updateById(rule);
        if (rows > 0) {
            /* 操作审计日志记录 */
            auditLogService.record("告警规则", "修改", rule.getRuleId(),
                    "修改告警规则：" + req.getRuleName());
        }
        return rows > 0;


    }

    /**
     * 删规则
     * @param ruleId
     * @return
     */
    @Override
    public boolean deleteRule(String ruleId) {
        AlarmRule rule = ruleMapper.selectByRuleId(ruleId);
        if (rule == null) throw new BusinessException(BusinessCode.NOT_FOUND_ERROR.getCode(), "规则不存在");
        rule.setDelFlag("1");
        rule.setUpdatedTime(LocalDateTime.now());
        int rows=ruleMapper.updateById(rule);
        if (rows > 0) {
            /* 操作审计日志记录 */
            auditLogService.record("告警规则", "删除", ruleId,
                    "删除告警规则：" + rule.getRuleName());
        }
        return rows > 0;
    }

    /**
     * 改规则启用
     * @param ruleId
     * @param enableStatus
     * @return
     */
    @Override
    public boolean toggleStatus(String ruleId, Boolean enableStatus) {
        AlarmRule rule = ruleMapper.selectByRuleId(ruleId);
        if (rule == null) throw new BusinessException(BusinessCode.NOT_FOUND_ERROR.getCode(), "规则不存在");
        rule.setEnableStatus(Boolean.TRUE.equals(enableStatus) ? "1" : "0");
        rule.setUpdatedTime(LocalDateTime.now());
        int rows=ruleMapper.updateById(rule);
        if (rows > 0) {
            /* 操作审计日志记录 */
            auditLogService.record("告警规则", "切换", ruleId,
                    "切换告警规则启用状态：" + rule.getRuleName() + " -> "
                            + (Boolean.TRUE.equals(enableStatus) ? "启用" : "禁用"));
        }
        return rows > 0;
    }



    /**
     * 单规则手动检测，异步提交：同步仅做规则校验并返回固定提示，
     * 检测任务异步执行，结果写入告警记录，不在响应中暴露检测结果
     *     1) 校验规则存在且启用
     *     2) 异步触发相关执行器采集，写 rms_metric_log
     *     3) 用采集数据判断该规则是否命中，命中写 alarm_record 并邮件通知
     * @param ruleId
     * @return
     */
    @Override
    public String triggerCheck(String ruleId) {
        AlarmRule rule = ruleMapper.selectByRuleId(ruleId);
        if (rule == null) {
            throw new BusinessException(BusinessCode.NOT_FOUND_ERROR.getCode(), "规则不存在");
        }
        if (!"1".equals(rule.getEnableStatus())) {
            throw new BusinessException(BusinessCode.OPERATION_ERROR.getCode(), "规则禁用，无法检测");
        }
        asyncTaskRunner.run(() -> {
            try {
                executeSingleCheck(ruleId);
                detectTaskRecorder.record("single-rule", true);
            } catch (Exception e) {
                log.error("[手动检测] 单规则检测任务执行失败, ruleId={}", ruleId, e);
                detectTaskRecorder.record("single-rule", false);
            }
        });
        return RESULT_HAS_ALARM_CHECK;
    }

    /**
     * 全规则手动检测，异步提交，同步返回固定提示。
     * @return
     */
    @Override
    public String triggerAllCheck() {
        asyncTaskRunner.run(() -> {
            try {
                executeAllCheck();
                detectTaskRecorder.record("all-rule", true);
            } catch (Exception e) {
                log.error("[手动检测] 全量检测任务执行失败", e);
                detectTaskRecorder.record("all-rule", false);
            }
        });
        return RESULT_HAS_ALARM_ALL;
    }

    /**
     * 单规则检测执行体（由 AlarmCheckExecutor 异步调用）
     */
    @Override
    public void executeSingleCheck(String ruleId) {
        AlarmRule rule = ruleMapper.selectByRuleId(ruleId);
        if (rule == null || !"1".equals(rule.getEnableStatus())) {
            return;
        }
        //解析规则作用范围的实例列表
        List<String> instanceIds = ruleMatchHelper.resolveRuleInstanceIds(rule);
        //触发相关执行器采集，写 rms_metric_log
        List<MetricReportItemDTO> collected = collectByInstanceIds(instanceIds);
        //用采集数据判断该规则
        checkAndGenerateAlarms(collected, Collections.singletonList(rule));
        //本次检测后，对该规则下的未恢复告警做自动恢复检查
        checkAndAutoRecover(Collections.singletonList(rule));
    }

    /**
     * 全规则检测执行体（由 AlarmCheckExecutor 异步调用）
     */
    @Override
    public void executeAllCheck() {
        List<AlarmRule> rules = ruleMapper.selectEnabledRules();
        if (rules == null || rules.isEmpty()) {
            return;
        }
        List<MetricReportItemDTO> collected = collectFromAllExecutors();
        checkAndGenerateAlarms(collected, rules);
        //本次全量检测后，对全部启用规则下的未恢复告警做自动恢复检查
        checkAndAutoRecover(rules);
    }

    @Override
    public RuleOverviewDTO getRuleOverview() {
        RuleOverviewDTO dto = ruleMapper.selectRuleOverview();
        if (dto == null) {
            dto = new RuleOverviewDTO();
        }
        /* 今日告警分级统计（由SQL扁平字段组装） */
        RuleOverviewDTO.TodayAlarmLevelCount levelCount = new RuleOverviewDTO.TodayAlarmLevelCount();
        levelCount.setInfo(dto.getTodayInfoCount() == null ? 0L : dto.getTodayInfoCount());
        levelCount.setAlarm(dto.getTodayAlarmCount() == null ? 0L : dto.getTodayAlarmCount());
        levelCount.setSeverity(dto.getTodaySeverityCount() == null ? 0L : dto.getTodaySeverityCount());
        dto.setTodayAlarmLevelCount(levelCount);
        /* 告警检测任务状态 */
        dto.setDetectTaskStatus(detectTaskRecorder.currentStatus());
        return dto;
    }



    /**
     * 触发指定实例对应的执行器采集，并将返回数据写入 rms_metric_log。
     * 同一执行器只触发一次。
     */
    private List<MetricReportItemDTO> collectByInstanceIds(List<String> instanceIds) {
        List<MetricReportItemDTO> collected = new ArrayList<>();
        if (instanceIds == null || instanceIds.isEmpty()) {
            return collected;
        }
        Set<String> visitedExecutorIds = new HashSet<>();

        for (String instanceId : instanceIds) {
            ExecutorInfo executor = findExecutorByInstanceId(instanceId);
            if (executor == null) {
                log.warn("[手动检测] 未找到实例 {} 对应的执行器", instanceId);
                continue;
            }
            if (!visitedExecutorIds.add(executor.getId())) {
                continue;
            }
            collected.addAll(triggerOneExecutor(executor));
        }
        return collected;
    }

    /**
     * 触发所有纳管执行器采集。
     */
    private List<MetricReportItemDTO> collectFromAllExecutors() {
        List<MetricReportItemDTO> collected = new ArrayList<>();
        List<ExecutorInfo> executors = executorInfoMapper.selectActiveOnlineExecutors("1", "1");
        if (executors == null || executors.isEmpty()) {
            return collected;
        }
        for (ExecutorInfo executor : executors) {
            collected.addAll(triggerOneExecutor(executor));
        }
        return collected;
    }

    /**
     * 调用单个执行器采集，成功时写入 rms_metric_log
     * @param executor
     * @return
     */
    private List<MetricReportItemDTO> triggerOneExecutor(ExecutorInfo executor) {
        List<MetricReportItemDTO> list = new ArrayList<>();
        try {
            AgentCollectResponse<List<MetricReportItemDTO>> resp =
                    agentHttpClient.triggerCollect(executor.getHostIp(), executor.getExecutorPort());
            if (resp == null || resp.getCode() != 0 || resp.getData() == null) {
                log.warn("[手动检测] 执行器 {} 采集失败: {}",
                        executor.getExecutorId(),
                        resp == null ? "null" : resp.getMessage());
                return list;
            }
            for (MetricReportItemDTO item : resp.getData()) {
                list.add(item);
                // 手动采集记录入表
                item.setToken(executor.getExecutorToken());
                item.setCollectType("1");
                try {
                    metricLogService.reportMetric(item);
                } catch (Exception e) {
                    log.error("[手动检测] 写入 rms_metric_log 失败, executor={}", executor.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("[手动检测] 调用执行器异常: {}", executor.getExecutorId(), e);
        }
        return list;
    }

    /**
     * 通过实例找到对应的执行器
     * @param instanceId
     * @return
     */
    private ExecutorInfo findExecutorByInstanceId(String instanceId) {
        return executorInfoMapper.selectByInstanceId(instanceId);
    }

    /**
     * 遍历规则 x 采集数据，命中阈值则生成告警记录
     * @param collected
     * @param rules
     * @return 是否存在任意一条告警
     */
    private boolean checkAndGenerateAlarms(List<MetricReportItemDTO> collected, List<AlarmRule> rules) {
        if (collected == null || collected.isEmpty() || rules == null || rules.isEmpty()) {
            return false;
        }
        boolean hasAlarm = false;
        for (AlarmRule rule : rules) {
            if (!"1".equals(rule.getEnableStatus())) {
                continue;
            }
            if (rule.getThreshold() == null || rule.getCompareType() == null) {
                continue;
            }
            for (MetricReportItemDTO item : collected) {
                if (!ruleMatchHelper.isRuleApplyToInstance(rule, item.getInstanceId())) {
                    continue;
                }

                //告警持续时长判断，默认 1个周期，触发就告警
                if (!isContinuousTriggered(rule, item.getInstanceId())) {
                    continue;
                }

                //静默期重复告警抑制
                if (isAlarmSilenced(rule, item.getInstanceId())) {
                    continue;
                }

                double value;
                try {
                    String valueStr = getMetricValue(item, rule.getMetricKey());
                    value = Double.parseDouble(valueStr.trim());
                } catch (Exception e) {
                    continue;
                }
                generateAlarmRecord(rule, item, value);
                hasAlarm = true;
            }
        }
        return hasAlarm;
    }

    /**
     * 持续触发判断，duration单位“采集周期个数”，默认1，看最新1条
     * 取该实例该指标最近 duration 条采集值，比较条件
     * @param rule
     * @param instanceId
     * @return
     */

    private boolean isContinuousTriggered(AlarmRule rule, String instanceId) {
        int duration = ParseUtils.parseDuration(rule.getDuration());
        if (duration <= 0) {
            duration = 1;
        }
        double threshold;
        try {
            threshold = Double.parseDouble(rule.getThreshold());
        } catch (Exception e) {
            return false;
        }

        List<String> values = metricLogMapper.selectRecentMetricValues(
                instanceId, rule.getMetricKey(), duration);
        if (values == null || values.size() < duration) {
            // 历史数据不足，暂不告警
            return false;
        }
        for (String v : values) {
            if (v == null || v.trim().isEmpty()) {
                return false;
            }
            try {
                double val = Double.parseDouble(v.trim());
                if (!CompareUtil.compareValue(val, rule.getCompareType(), threshold)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 静默期重复告警抑制 silenceDuration 单位秒
     * @param rule
     * @param instanceId
     * @return
     */
    private boolean isAlarmSilenced(AlarmRule rule, String instanceId) {
        String silenceDuration = rule.getSilenceDuration();
        if (silenceDuration == null || silenceDuration.trim().isEmpty()) {
            return false;
        }
        long seconds;
        try {
            seconds = Long.parseLong(silenceDuration.trim());
        } catch (Exception e) {
            return false;
        }
        if (seconds <= 0) {
            return false;
        }
        LocalDateTime since = LocalDateTime.now().minusSeconds(seconds);
        long count = alarmRecordMapper.countRecentByRuleAndInstance(
                rule.getRuleId(), instanceId, since);
        return count > 0;
    }



    /**
     * 从上报项中取指标值。
     */
    private String getMetricValue(MetricReportItemDTO item, String metricKey) {
        if (metricKey == null) {
            return null;
        }
        switch (metricKey) {
            case "cpuUsage":
                return item.getCpuPer();
            case "serverMemUsage":
                return item.getMemServerPer();
            case "redisMemUsed":
                return item.getMemUs();
            case "qps":
                return item.getOps();
            case "avgLatency":
                return item.getLatency();
            case "netInputBandwidth":
                return item.getIoIik();
            case "netOutputBandwidth":
                return item.getIoIok();
            case REPL_DELAY_METRIC_KEY:
                return item.getReplDelay();
            default:
                return null;
        }
    }

    /**
     * 生成一条告警记录
     * @param rule
     * @param item
     * @param value
     */
    private void generateAlarmRecord(AlarmRule rule, MetricReportItemDTO item, double value) {
        try {
            AlarmRecord record = new AlarmRecord();
            record.setAlarmNo("AL" + System.currentTimeMillis()
                    + String.format("%03d", (int) (Math.random() * 1000)));
            record.setRuleId(rule.getRuleId());
            record.setInstanceId(item.getInstanceId());
            record.setMetricKey(rule.getMetricKey());
            record.setMetricName(rule.getMetricName());
            record.setAlarmLevel(rule.getAlarmLevel());
            record.setAlarmContent(String.format("指标【%s】当前值 %s%s 超过阈值 %s%s",
                    rule.getMetricName() == null ? rule.getMetricKey() : rule.getMetricName(),
                    value, rule.getUnit() == null ? "" : rule.getUnit(),
                    rule.getThreshold(), rule.getUnit() == null ? "" : rule.getUnit()));
            record.setTriggerValue(String.valueOf(value));
            record.setThreshold(rule.getThreshold());
            record.setUnit(rule.getUnit());
            record.setTriggerTime(LocalDateTime.now());
            record.setRecoverStatus(StatusEnum.UNRECOVER.getCode());
            record.setCreatedTime(LocalDateTime.now());
            record.setUpdatedTime(LocalDateTime.now());
            alarmRecordMapper.insert(record);
            /* 生成告警记录后邮件通知，并记录通知发送结果 */
            alarmNoticeService.sendAlarmNotice(record, rule);
        } catch (Exception e) {
            log.error("[手动检测] 生成告警记录失败, ruleId={}", rule.getRuleId(), e);
        }
    }

    // ========================================================================
    // [新增] 告警自动恢复：指标连续 N 个采集周期回落阈值以下自动恢复
    // ========================================================================

    /**
     * 自动恢复检查：
     * 1) 开关关闭（periods<=0）直接返回；
     * 2) 收集本次检测涉及的 ruleId，一次性查询这些规则下的未恢复告警（自定义 Mapper）；
     * 3) 逐条判断最近 N 条采集值是否全部不满足告警条件，满足则自动恢复。
     *
     * @param rules 本次检测涉及的规则列表（单规则=单条，全量=全部启用规则）
     */
    private void checkAndAutoRecover(List<AlarmRule> rules) {
        // [新增] 自动恢复开关：periods<=0 视为关闭
        if (autoRecoverPeriods <= 0) {
            return;
        }
        if (rules == null || rules.isEmpty()) {
            return;
        }

        // [新增] 构建 ruleId -> rule 映射（for 循环，不用 Stream）
        Map<String, AlarmRule> ruleMap = new HashMap<>();
        List<String> ruleIds = new ArrayList<>(rules.size());
        for (AlarmRule rule : rules) {
            if (rule == null || rule.getRuleId() == null) {
                continue;
            }
            if (ruleMap.put(rule.getRuleId(), rule) == null) {
                ruleIds.add(rule.getRuleId());
            }
        }
        if (ruleIds.isEmpty()) {
            return;
        }

        // [新增] 自定义 Mapper 一次性查询这些规则下的未恢复告警，避免 N 次单规则查询
        List<AlarmRecord> unrecovered = alarmRecordMapper.selectUnrecoveredByRuleIds(ruleIds);
        if (unrecovered == null || unrecovered.isEmpty()) {
            return;
        }

        // [新增] 逐条判断是否满足自动恢复条件
        for (AlarmRecord record : unrecovered) {
            if (record == null || record.getRuleId() == null) {
                continue;
            }
            AlarmRule rule = ruleMap.get(record.getRuleId());
            if (rule == null) {
                // 规则不在本次检测范围（已禁用/已删除），跳过，保留人工恢复入口
                continue;
            }
            try {
                if (isRecoveredByRecentValues(rule, record)) {
                    autoRecover(record);
                }
            } catch (Exception e) {
                log.error("[自动恢复] 检查失败, alarmRecordId={}", record.getId(), e);
            }
        }
    }

    /**
     * 判断告警是否满足自动恢复条件：
     * 取该实例该指标最近 autoRecoverPeriods 条采集值（倒序），
     * 全部不满足告警条件则视为已恢复。
     * 以下情况保守返回 false（不恢复）：
     * - 规则阈值/比较方式缺失或解析失败；
     * - 采集值不足 N 条；
     * - 任意一条为 null / 空串 / 解析失败；
     * - 任意一条仍满足告警条件。
     */
    private boolean isRecoveredByRecentValues(AlarmRule rule, AlarmRecord record) {
        if (rule.getThreshold() == null || rule.getCompareType() == null) {
            return false;
        }
        double threshold;
        try {
            threshold = Double.parseDouble(rule.getThreshold());
        } catch (Exception e) {
            return false;
        }
        // [新增] 复用 MetricLogMapper 自定义方法取最近 N 条采集值（倒序）
        List<String> values = metricLogMapper.selectRecentMetricValues(
                record.getInstanceId(), rule.getMetricKey(), autoRecoverPeriods);
        if (values == null || values.size() < autoRecoverPeriods) {
            return false;
        }
        // [新增] for 循环判断，任意一条仍满足告警条件则未恢复
        for (String v : values) {
            if (v == null || v.trim().isEmpty()) {
                return false;
            }
            try {
                double val = Double.parseDouble(v.trim());
                if (CompareUtil.compareValue(val, rule.getCompareType(), threshold)) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 执行自动恢复：
     * 更新 recover_status=1、recover_time=当前时间、updated_time=当前时间；
     * 记入操作审计日志（operationType=自动恢复，与人工恢复区分）。
     * 使用 BaseMapper.updateById（非 LambdaWrapper），传入部分字段，
     * MyBatis-Plus 默认 NOT_NULL 更新策略只更新非 null 字段。
     */
    private void autoRecover(AlarmRecord record) {
        LocalDateTime now = LocalDateTime.now();
        AlarmRecord update = new AlarmRecord();
        update.setId(record.getId());
        update.setRecoverStatus(StatusEnum.RECOVER.getCode());
        update.setRecoverTime(now);
        update.setUpdatedTime(now);
        int rows = alarmRecordMapper.updateById(update);
        if (rows > 0) {
            // [新增] 自动恢复记入操作审计日志
            auditLogService.record("告警记录", "自动恢复", record.getId(),
                    "指标连续" + autoRecoverPeriods + "周期回落阈值以下，自动恢复告警："
                            + record.getAlarmNo());
            log.info("[自动恢复] 告警已自动恢复, alarmNo={}, periods={}",
                    record.getAlarmNo(), autoRecoverPeriods);
        }
    }


}



