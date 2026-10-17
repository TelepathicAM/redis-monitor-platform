package com.danuvius.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.danuvius.platform.common.exception.BusinessCode;
import com.danuvius.platform.common.exception.BusinessException;
import com.danuvius.platform.mapper.*;
import com.danuvius.platform.model.dto.metric.MetricCurrentValueDTO;
import com.danuvius.platform.model.dto.monitor.*;
import com.danuvius.platform.model.entity.AlarmRule;
import com.danuvius.platform.model.entity.MetricLog;
import com.danuvius.platform.model.entity.RedisInstance;
import com.danuvius.platform.model.entity.RedisSwitchEvent;
import com.danuvius.platform.model.enums.AlarmLevelEnum;
import com.danuvius.platform.model.enums.MetricTypeEnum;
import com.danuvius.platform.model.enums.StatusEnum;
import com.danuvius.platform.model.enums.TypeEnum;
import com.danuvius.platform.service.AuditLogService;
import com.danuvius.platform.service.RedisInstanceService;
import com.danuvius.platform.service.ReportExportExecutor;
import com.danuvius.platform.util.*;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
public class RedisInstanceServiceImpl extends ServiceImpl<RedisInstanceMapper, RedisInstance>
        implements RedisInstanceService {
    /** 复制延迟指标 key，与前端/规则保持一致 */
    private static final String REPL_DELAY_METRIC_KEY = "replDelay";

    /* 实例离线判定阈值：超过该时长无心跳视为离线，平台端+执行器架构下按心跳时长判定，默认600s，可配置） */
    @Value("${platform.instance.offline-threshold-seconds:600}")
    private long offlineThresholdSeconds;

    private final RedisInstanceMapper instanceMapper;
    private final RedisSwitchEventMapper switchEventMapper;
    private final AlarmRuleMapper alarmRuleMapper;
    private final AlarmRecordMapper alarmRecordMapper;
    private final MetricLogMapper metricLogMapper;
    private final RuleMatchHelper  ruleMatchHelper;
    /* 报告导出记操作审计日志 */
    private final AuditLogService auditLogService;
    /* 报告导出异步任务存储 */
    private final ReportExportTaskHolder taskHolder;
    /* 报告导出异步执行器（独立Bean保证@Async生效） */
    private final ReportExportExecutor reportExportExecutor;

    private final Cache<String, Object> caffeineCache;

    public RedisInstanceServiceImpl(RedisInstanceMapper instanceMapper,
                                    RedisSwitchEventMapper switchEventMapper,
                                    AlarmRuleMapper alarmRuleMapper,
                                    AlarmRecordMapper alarmRecordMapper,
                                    MetricLogMapper metricLogMapper,
                                    RuleMatchHelper ruleMatchHelper,
                                    AuditLogService auditLogService,
                                    ReportExportTaskHolder taskHolder,
                                    ReportExportExecutor reportExportExecutor,
                                    Cache<String, Object> caffeineCache
    ) {
        this.instanceMapper = instanceMapper;
        this.switchEventMapper = switchEventMapper;
        this.alarmRuleMapper = alarmRuleMapper;
        this.alarmRecordMapper = alarmRecordMapper;
        this.metricLogMapper = metricLogMapper;
        this.ruleMatchHelper = ruleMatchHelper;
        this.auditLogService = auditLogService;
        this.taskHolder = taskHolder;
        this.reportExportExecutor = reportExportExecutor;
        this.caffeineCache = caffeineCache;
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 查询实例列表。
     */
    @Override
    public List<RedisInstanceItemDTO> getInstanceList(String managementStatus) {
        String key = CacheKeyUtil.instanceList(managementStatus);
        Object cached = caffeineCache.getIfPresent(key);
        if (cached != null) {
            return (List<RedisInstanceItemDTO>) cached;
        }

        List<RedisInstanceItemDTO> list = instanceMapper.selectInstanceList(managementStatus);
        /*无心跳或心跳超时（默认70s."连续2次采集无心跳响应则离线"）为离线,有心跳且状态不是警告/严重时，正常*/
        LocalDateTime offlineBefore = LocalDateTime.now().minusSeconds(offlineThresholdSeconds);
        for (RedisInstanceItemDTO row : list) {
            row.setRunStatus(StatusEnum.descOf(row.getRunStatus()));
            //无心跳、心跳超时
            if (row.getLastHeartbeatTime() == null || row.getLastHeartbeatTime().isBefore(offlineBefore)) {
                row.setRunStatus(StatusEnum.RUN_OFFLINE.getDesc());
            } else if (row.getRunStatus() == null
                    || (!StatusEnum.RUN_CRITICAL.getDesc().equals(row.getRunStatus())
                    && !StatusEnum.RUN_WARNING.getDesc().equals(row.getRunStatus()))) {
                row.setRunStatus(StatusEnum.RUN_NORMAL.getDesc());
            }
        }
        caffeineCache.put(key, list);
        return list;
    }

    /**
     * 查询核心实时指标
     * @param instanceId
     * @return
     */
    @Override
    public MetricCurrentValueResponse getCurrentValues(String instanceId) {
        RedisInstance inst = instanceMapper.selectById(instanceId);
        if (inst == null) {
            throw new BusinessException(BusinessCode.NOT_FOUND_ERROR.getCode(), "实例不存在");
        }

        String key = CacheKeyUtil.realtimeCurrent(instanceId);
        Object cached = caffeineCache.getIfPresent(key);
        if (cached != null) {
            return (MetricCurrentValueResponse) cached;
        }

        List<Map<String, Object>> latestList = instanceMapper.selectLatestMetric(instanceId);
        Map<String, Object> latest = (latestList == null || latestList.isEmpty())
                ? Collections.emptyMap() : latestList.get(0);
        if (latest == null) {
            latest = Collections.emptyMap();
        }

        List<Map<String, Object>> prevList = instanceMapper.selectPrevMetric(instanceId);
        Map<String, Object> prev = (prevList == null || prevList.isEmpty())
                ? Collections.emptyMap() : prevList.get(0);

        if (prev == null) {
            prev = Collections.emptyMap();
        }

        // 查询启用中的、适用于当前实例的告警规则
        List<AlarmRule> boundRules = alarmRuleMapper.selectEnabledRulesByInstance(instanceId);
        if (boundRules == null) {
            boundRules = Collections.emptyList();
        }

        List<MetricCurrentValueDTO> metrics = new ArrayList<>();

        for (MetricTypeEnum metricType : MetricTypeEnum.values()) {
            MetricCurrentValueDTO m = new MetricCurrentValueDTO();
            m.setMetricKey(metricType.getKey());
            m.setMetricName(metricType.getName());
            m.setUnit(metricType.getUnit());

            //新值
            Object cur = null;
            if (latest != null) {
                cur = latest.get(metricType.getKey());
            }
            m.setCurrentValue(cur);

            //判断是否有数据
            if (latest == null || cur == null) {
                m.setAlarmLevel(AlarmLevelEnum.NO_DATA.getDesc());
            } else {
                m.setAlarmLevel(judgeMetricStatus(cur, metricType.getKey(), boundRules));
            }

            //旧值
            Object pv = null;
            if (prev != null) {
                pv = prev.get(metricType.getKey());
            }
            //(新-旧)/旧*100
            if (cur != null && pv != null) {
                try {
                    double c = Double.parseDouble(cur.toString());
                    double p = Double.parseDouble(pv.toString());
                    if (p != 0) {
                        m.setChangeRate5min((c - p) / p * 100);
                    }
                } catch (Exception ignored) {
                    //非数字指标忽略
                }
            }
            metrics.add(m);
        }

        MetricCurrentValueResponse resp = new MetricCurrentValueResponse();
        resp.setMetrics(metrics);
        caffeineCache.put(key, resp);
        return resp;
    }

    /**
     * 根据告警规则判断指标状态，返回指标中文描述
     * @param currentVal
     * @param metricKey
     * @param rules
     * @return
     */
    private String judgeMetricStatus(Object currentVal, String metricKey, List<AlarmRule> rules) {
        double value;
        try {
            value = Double.parseDouble(currentVal.toString());
        } catch (Exception e) {
            return AlarmLevelEnum.NORMAL.getDesc();
        }

        Double maxLevel = ParseUtils.stringToDouble(AlarmLevelEnum.NORMAL.getCode());

        for (AlarmRule rule : rules) {
            if (!metricKey.equals(rule.getMetricKey()) || rule.getThreshold() == null) {
                continue;
            }

            double threshold;
            try {
                threshold = Double.parseDouble(rule.getThreshold());
            } catch (Exception e) {
                continue;
            }
            String compareType = rule.getCompareType();
            boolean triggered = false;
            triggered=CompareUtil.compareValue(value,compareType,threshold);

            if (triggered && rule.getAlarmLevel() != null) {
                Double level = ParseUtils.stringToDouble(AlarmLevelEnum.codeOf((rule.getAlarmLevel())));
                if (level > maxLevel) {
                    maxLevel = level;
                }
            }
        }

        return AlarmLevelEnum.descOf(ParseUtils.doubleToString(maxLevel));
    }

    /**
     * 多节点指标趋势
     */
    @Override
    public TrendResponse getTrend(TrendRequest req) {
        String key = CacheKeyUtil.realtimeTrend(req.getMetricKey(), req.getInstanceIds(), req.getTimeRange());
        Object cached = caffeineCache.getIfPresent(key);
        if (cached != null) {
            return (TrendResponse) cached;
        }

        /*确定聚合粒度：近1h=5min、近6h=15min*/
        boolean near1h = "近1h".equals(req.getTimeRange());
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = near1h ? endTime.minusHours(1) : endTime.minusHours(6);
        int bucketMinutes = near1h ? 5 : 15;

        /*网络带宽同时返回出入带宽两条子序列*/
        boolean dualBandwidth = MetricTypeEnum.NET_INPUT_BANDWIDTH.getKey().equals(req.getMetricKey())
                || MetricTypeEnum.NET_OUTPUT_BANDWIDTH.getKey().equals(req.getMetricKey());

        List<Map<String, Object>> rows = instanceMapper.selectTrendData(
                req.getInstanceIds(), req.getMetricKey(), bucketMinutes, dualBandwidth,
                startTime.format(FORMATTER), endTime.format(FORMATTER));

        TrendResponse resp = new TrendResponse();
        MetricTypeEnum metricType = MetricTypeEnum.of(req.getMetricKey());
        if (metricType != null) {
            resp.setMetricName(metricType.getName());
            resp.setUnit(metricType.getUnit());
        } else {
            resp.setMetricName("");
        }
        if (dualBandwidth) {
            resp.setMetricName("网络入/出带宽");
            resp.setUnit(MetricTypeEnum.NET_INPUT_BANDWIDTH.getUnit());
        }

        /* 统一时间轴：生成 start~end 按粒度的完整时间点，缺失点补 null 供前端断点展示 */
        List<String> timestampList = buildAlignedTimestamps(startTime, endTime, bucketMinutes);
        resp.setTimestampList(timestampList);

        // 按实例分组
        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String iid = String.valueOf(row.get("instanceId"));
            List<Map<String, Object>> points = grouped.get(iid);
            if (points == null) {
                points = new ArrayList<>();
                grouped.put(iid, points);
            }
            points.add(row);
        }

        List<TrendSeriesDTO> series = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
            if (dualBandwidth) {
                series.add(buildAlignedSeries(entry.getKey(), MetricTypeEnum.NET_INPUT_BANDWIDTH.getKey(),
                        entry.getValue(), timestampList, "inValue"));
                series.add(buildAlignedSeries(entry.getKey(), MetricTypeEnum.NET_OUTPUT_BANDWIDTH.getKey(),
                        entry.getValue(), timestampList, "outValue"));
            } else {
                series.add(buildAlignedSeries(entry.getKey(), req.getMetricKey(),
                        entry.getValue(), timestampList, "value"));
            }
        }
        resp.setSeriesList(series);
        caffeineCache.put(key, resp);
        return resp;
    }

    /**
     * 生成对齐后的统一时间轴（yyyy-MM-dd HH:mm:ss，升序）。
     */
    private List<String> buildAlignedTimestamps(LocalDateTime start, LocalDateTime end, int bucketMinutes) {
        List<String> timestamps = new ArrayList<>();
        LocalDateTime cursor = truncateToBucket(start, bucketMinutes);
        while (!cursor.isAfter(end)) {
            timestamps.add(cursor.format(FORMATTER));
            cursor = cursor.plusMinutes(bucketMinutes);
        }
        return timestamps;
    }

    /**
     * 将时间按分桶粒度向下取整（与 SQL bucketExpr 口径一致）。
     */
    private LocalDateTime truncateToBucket(LocalDateTime time, int bucketMinutes) {
        int minuteOfHour = time.getMinute();
        int aligned = (minuteOfHour / bucketMinutes) * bucketMinutes;
        return time.withMinute(aligned).withSecond(0).withNano(0);
    }

    /**
     * 将单实例采集点对齐到统一时间轴，缺失点补 null。
     *
     * @param valueColumn 值列名：value / inValue / outValue
     */
    private TrendSeriesDTO buildAlignedSeries(String instanceId, String metricKey,
                                              List<Map<String, Object>> points,
                                              List<String> timestampList, String valueColumn) {
        TrendSeriesDTO s = new TrendSeriesDTO();
        s.setInstanceId(instanceId);
        s.setMetricKey(metricKey);

        // bucket -> value
        Map<String, Object> valueMap = new HashMap<>();
        for (Map<String, Object> point : points) {
            valueMap.put(formatBucket(point.get("bucket")), point.get(valueColumn));
        }

        List<Map<String, Object>> alignedPoints = new ArrayList<>();
        for (String ts : timestampList) {
            Map<String, Object> p = new HashMap<>();
            p.put("bucket", ts);
            p.put("value", valueMap.get(ts));
            alignedPoints.add(p);
        }
        s.setPoints(alignedPoints);
        return s;
    }

    /**
     * 统一时间桶格式：JDBC 可能返回 Timestamp/LocalDateTime/String，统一为 yyyy-MM-dd HH:mm:ss。
     */
    private String formatBucket(Object bucket) {
        if (bucket == null) {
            return null;
        }
        if (bucket instanceof LocalDateTime) {
            return ((LocalDateTime) bucket).format(FORMATTER);
        }
        if (bucket instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) bucket).toLocalDateTime().format(FORMATTER);
        }
        if (bucket instanceof Date) {
            return new java.sql.Timestamp(((Date) bucket).getTime()).toLocalDateTime().format(FORMATTER);
        }
        String text = bucket.toString();
        // 兼容 "2026-10-15 10:05:00.0" 形式
        if (text.length() > 19) {
            text = text.substring(0, 19);
        }
        return text;
    }



    /**
     * 主从拓扑。
     */
    @Override
    public TopologyResponse getTopology() {
        Object cached = caffeineCache.getIfPresent(CacheKeyUtil.TOPOLOGY);
        if (cached != null) {
            return (TopologyResponse) cached;
        }

        List<RedisInstance> instances = instanceMapper.selectActiveInstances("1");
        List<AlarmRule> allRules = alarmRuleMapper.selectEnabledRules();
        if (allRules == null) {
            allRules = Collections.emptyList();
        }

        List<TopologyNodeDTO> nodes = new ArrayList<>();
        boolean masterOnline = false;
        boolean anySlaveDelay = false;

        /* 用实例列表构建 host:port -> instanceId 映射，反查主节点实例id（ */
        Map<String, String> hostPortToInstanceId = new HashMap<>();
        for (RedisInstance inst : instances) {
            hostPortToInstanceId.put(inst.getHostIp() + ":" + inst.getRedisPort(), inst.getInstanceId());
        }

        for (RedisInstance inst : instances) {
            TopologyNodeDTO n = new TopologyNodeDTO();
            n.setInstanceId(inst.getInstanceId());
            n.setInstanceName(inst.getInstanceName());
            n.setIp(inst.getHostIp());
            n.setPort(inst.getRedisPort());
            n.setRole(inst.getRole());

            String ip = inst.getMasterHostIp() != null ? inst.getMasterHostIp() : "null";
            String port = inst.getMasterRedisPort() != null ? inst.getMasterRedisPort() : "null";
            String masterInstanceId = hostPortToInstanceId.get(ip + ":" + port);
            n.setMasterName((masterInstanceId != null ? masterInstanceId : ip) + ":" + port);

            //从 rms_metric_log 最新记录获取复制延迟（按 executor/instance）
            String replDelay = getLatestReplDelay(inst.getInstanceId());
            n.setCopyDelay(replDelay != null ? replDelay : "0");

            //根据告警规则判断复制状态
            String copyStatus = judgeReplDelayStatus(inst.getInstanceId(), replDelay, allRules);
            n.setCopyStatus(copyStatus);

            boolean isMaster = "MASTER".equalsIgnoreCase(inst.getRole());
            if (isMaster) {
                masterOnline = isOnline(inst);
            } else if ("延迟".equals(copyStatus)) {
                anySlaveDelay = true;
            }
            nodes.add(n);
        }
        String topologyStatus;
        if (masterOnline && !anySlaveDelay) {
            topologyStatus = "健康";
        } else if (masterOnline && anySlaveDelay) {
            topologyStatus = "警告";
        } else {
            topologyStatus = "异常";
        }


        // 最近一次切换事件
        RedisSwitchEvent lastEvent = switchEventMapper.selectLatestSwitchEvent();

        TopologyResponse resp = new TopologyResponse();
        resp.setNodes(nodes);
        resp.setStatus(topologyStatus);
        resp.setLastSwitch(lastEvent);

        caffeineCache.put(CacheKeyUtil.TOPOLOGY, resp);
        return resp;
    }

    /**
     * 主从状态表格。
     */
    @Override
    public List<NodeStatusResponse> getNodesStatus() {
        Object cached = caffeineCache.getIfPresent(CacheKeyUtil.NODES);
        if (cached != null) {
            @SuppressWarnings("unchecked")
            List<NodeStatusResponse> ret = (List<NodeStatusResponse>) cached;
            return ret;
        }

        List<RedisInstance> instances = instanceMapper.selectActiveInstancesOrderByName("1");
        List<AlarmRule> allRules = alarmRuleMapper.selectEnabledRules();
        if (allRules == null) {
            allRules = Collections.emptyList();
        }

        List<NodeStatusResponse> masterRows = new ArrayList<>();
        List<NodeStatusResponse> slaveRows = new ArrayList<>();

        /* 用实例列表构建 host:port -> instanceId 映射，反查主节点实例id */
        Map<String, String> hostPortToInstanceId = new HashMap<>();
        for (RedisInstance i : instances) {
            hostPortToInstanceId.put(i.getHostIp() + ":" + i.getRedisPort(), i.getInstanceId());
        }

        String masterIp = null;
        String masterPort = null;
        for (RedisInstance i : instances) {
            NodeStatusResponse m = new NodeStatusResponse();
            m.setInstanceId(i.getInstanceId());
            m.setInstanceName(i.getInstanceName());
            m.setIp(i.getHostIp());
            m.setPort(i.getRedisPort());
            m.setRole(i.getRole());

            // masterName 拼接为 host:port
            masterIp = i.getMasterHostIp() == null ? "null" : i.getMasterHostIp();
            masterPort = i.getMasterRedisPort() == null ? "null" : i.getMasterRedisPort();
            String masterInstanceId = hostPortToInstanceId.get(masterIp + ":" + masterPort);
            m.setMasterName(masterInstanceId != null ? masterInstanceId : masterIp);

            //复制延迟从 rms_metric_log 最新记录获取
            String replDelay = getLatestReplDelay(i.getInstanceId());
            m.setCopyDelay(replDelay != null ? replDelay : "0");
            //复制状态由告警规则判断
            m.setCopyStatus(judgeReplDelayStatus(i.getInstanceId(), replDelay, allRules));
            m.setRunStatus(StatusEnum.descOf(i.getRunStatus()));
            if ("MASTER".equalsIgnoreCase(i.getRole())) {
                masterRows.add(m);
            } else {
                slaveRows.add(m);
            }

        }
        //从节点按实例名升序
        slaveRows.sort(Comparator.comparing(NodeStatusResponse::getInstanceName,
                Comparator.nullsLast(String::compareTo)));

        List<NodeStatusResponse> result = new ArrayList<>(masterRows.size() + slaveRows.size());
        result.addAll(masterRows);
        result.addAll(slaveRows);

        caffeineCache.put(CacheKeyUtil.NODES, result);
        return result;
    }

    /**
     * 日周报查询。
     * 当天=00:00~当前时刻、近7天=前推6天共7自然日，
     */
    @Override
    public ReportQueryResponse queryReport(ReportQueryRequest req) {
        String key = CacheKeyUtil.report(req.getInstanceId(), req.getReportType());
        Object cached = caffeineCache.getIfPresent(key);
        if (cached != null) {
            return (ReportQueryResponse) cached;
        }

        ReportQueryResponse resp = buildReportData(req.getInstanceId(), req.getReportType());

        caffeineCache.put(key, resp);
        return resp;
    }

    /**
     * 组装完整报告数据，查询与导出共用。
     */
    @Override
    public ReportQueryResponse buildReportData(String instanceId, String reportType) {
        boolean daily = !"周报".equals(reportType);
        LocalDateTime endTime = LocalDateTime.now();
        // 当天报告：00:00 至当前时刻；近7天报告：当前日期向前推6天到当天共7自然日
        LocalDateTime startTime = daily
                ? endTime.toLocalDate().atStartOfDay()
                : endTime.toLocalDate().minusDays(6).atStartOfDay();
        String startText = startTime.format(FORMATTER);
        String endText = endTime.format(FORMATTER);

        ReportQueryResponse resp = new ReportQueryResponse();

        // 概览统计
        List<Map<String, Object>> statsList = instanceMapper.selectReportOverviewStats(
                instanceId, startText, endText);
        Map<String, Object> stats = (statsList == null || statsList.isEmpty())
                ? Collections.emptyMap() : statsList.get(0);

        ReportOverviewDTO ov = new ReportOverviewDTO();
        ov.setCpuAvg(toDouble(stats.get("cpuAvg")));
        ov.setCpuMax(toDouble(stats.get("cpuMax")));
        ov.setCpuMaxTim(toLocalDateTime(stats.get("cpuMaxTim")));
        ov.setRedisMemAvg(toDouble(stats.get("redisMemAvg")));
        ov.setRedisMemMax(toDouble(stats.get("redisMemMax")));
        ov.setRedisMemMaxTim(toLocalDateTime(stats.get("redisMemMaxTim")));
        ov.setQpsAvg(toDouble(stats.get("qpsAvg")));
        ov.setQpsMax(toDouble(stats.get("qpsMax")));
        ov.setQpsMaxTim(toLocalDateTime(stats.get("qpsMaxTim")));
        ov.setLatencyAvg(toDouble(stats.get("latencyAvg")));
        ov.setLatencyMax(toDouble(stats.get("latencyMax")));
        ov.setLatencyMaxTim(toLocalDateTime(stats.get("latencyMaxTim")));

        // 告警统计（按时间范围，可按实例过滤）
        Map<String, Object> alarmStats = alarmRecordMapper.selectReportAlarmStats(
                instanceId, startText, endText);
        if (alarmStats == null) {
            alarmStats = Collections.emptyMap();
        }
        ov.setAlarmTotal(toLong(alarmStats.get("alarmTotal")));
        ReportOverviewDTO.AlarmSeverityCountDTO severity = new ReportOverviewDTO.AlarmSeverityCountDTO();
        severity.setInfo(toLong(alarmStats.get("infoCount")));
        severity.setAlarm(toLong(alarmStats.get("alarmCount")));
        severity.setSeverity(toLong(alarmStats.get("severityCount")));
        ov.setAlarmSeverityCount(severity);

        // 主从切换统计
        Map<String, Object> switchStats = switchEventMapper.selectSwitchStats(startText, endText);
        if (switchStats == null) {
            switchStats = Collections.emptyMap();
        }
        ov.setSwitchTotal(toLong(switchStats.get("switchTotal")));
        ov.setLastSwitchTime(toLocalDateTime(switchStats.get("lastSwitchTime")));

        // 节点可用率：实际采集点数 / 理论采集点数（5分钟一个采集周期）
        long collectCount = toLong(stats.get("collectCount")) == null ? 0L : toLong(stats.get("collectCount"));
        long theoryMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
        long theoryCount = Math.max(1L, theoryMinutes / 5);
        BigDecimal rate = BigDecimal.valueOf(collectCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(theoryCount), 2, RoundingMode.HALF_UP);
        if (rate.compareTo(BigDecimal.valueOf(100)) > 0) {
            rate = BigDecimal.valueOf(100);
        }
        ov.setServiceAvailableRate(rate);

        // 主/从节点数（集群口径）
        List<RedisInstance> allInstances = instanceMapper.selectActiveInstances("1");
        long masterCount = 0;
        long replicaCount = 0;
        if (allInstances != null) {
            for (RedisInstance instance : allInstances) {
                if ("MASTER".equalsIgnoreCase(instance.getRole())) {
                    masterCount++;
                } else {
                    replicaCount++;
                }
            }
        }
        ov.setMasterCount(masterCount);
        ov.setReplicaCount(replicaCount);

        resp.setOverview(ov);

        // 4项指标趋势序列
        // 当天按小时聚合，近7天按天聚合
        int bucketMinutes = daily ? 60 : 1440;
        boolean truncateDay = !daily;
        List<Map<String, Object>> trendRows = instanceMapper.selectReportTrend(
                instanceId, startText, endText, bucketMinutes, truncateDay);
        resp.setTrendList(buildReportTrends(trendRows, startTime, endTime, bucketMinutes));

        // 告警与切换事件合并列表
        List<Map<String, Object>> alarmRows = alarmRecordMapper.selectReportEventList(
                instanceId, startText, endText, 500);
        List<Map<String, Object>> switchRows = switchEventMapper.selectSwitchEventList(
                startText, endText, 500);
        resp.setEventList(buildReportEvents(alarmRows, switchRows));

        return resp;
    }

    /**
     * 组装4项指标趋势序列，时间轴对齐、缺失补 null。
     */
    private List<ReportTrendDTO> buildReportTrends(List<Map<String, Object>> rows,
                                                   LocalDateTime start, LocalDateTime end,
                                                   int bucketMinutes) {
        List<ReportTrendDTO> trends = new ArrayList<>();
        List<String> timestampList = buildAlignedTimestamps(start, end, bucketMinutes);

        String[][] metricDefs = {
                {"cpuUsage", "CPU使用率", "%"},
                {"redisMemUsed", "Redis内存使用量", "GB"},
                {"qps", "QPS", "次/秒"},
                {"avgLatency", "平均延迟", "ms"}
        };

        // bucket -> 各指标值
        Map<String, Map<String, Object>> bucketMap = new HashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                bucketMap.put(formatBucket(row.get("bucket")), row);
            }
        }

        for (String[] def : metricDefs) {
            ReportTrendDTO trend = new ReportTrendDTO();
            trend.setMetricKey(def[0]);
            trend.setMetricName(def[1]);
            trend.setUnit(def[2]);
            List<Double> values = new ArrayList<>();
            for (String ts : timestampList) {
                Map<String, Object> row = bucketMap.get(ts);
                values.add(row == null ? null : toDouble(row.get(def[0])));
            }
            trend.setTimestampList(new ArrayList<>(timestampList));
            trend.setValueList(values);
            trends.add(trend);
        }
        return trends;
    }

    /**
     * 合并告警与主从切换明细，按触发时间倒序，补充节点展示字段。
     */
    private List<ReportEventDTO> buildReportEvents(List<Map<String, Object>> alarmRows,
                                                   List<Map<String, Object>> switchRows) {
        List<ReportEventDTO> events = new ArrayList<>();
        if (alarmRows != null) {
            for (Map<String, Object> row : alarmRows) {
                ReportEventDTO event = new ReportEventDTO();
                event.setEventType(TypeEnum.ALARM.getDesc());
                String instanceId = (String) row.get("instanceId");
                event.setNodeDisplay((instanceId == null ? "" : instanceId));
                event.setContent(str(row.get("alarmContent")));
                event.setHappenTime(toLocalDateTime(row.get("triggerTime")));
                event.setStatus("1".equals(row.get("recoverStatus")) ? StatusEnum.RECOVER.getDesc() : StatusEnum.UNRECOVER.getDesc());
                events.add(event);
            }
        }
        if (switchRows != null) {
            for (Map<String, Object> row : switchRows) {
                ReportEventDTO event = new ReportEventDTO();
                event.setEventType(TypeEnum.MASTER_SWITCH.getDesc());
                event.setNodeDisplay(row.get("oldInstanceName") + " → " + row.get("newInstanceName"));
                event.setContent(event.getEventType()+"：" + event.getNodeDisplay()
                        + "（" + TypeEnum.descOf(str(row.get("switchType"))) + "）");
                event.setHappenTime(toLocalDateTime(row.get("switchTime")));
                event.setStatus("1".equals(row.get("switchResult")) ? StatusEnum.SUCCESS.getCode() : StatusEnum.FAILED.getCode());
                events.add(event);
            }
        }
        // 按发生时间倒序
        events.sort((a, b) -> {
            if (a.getHappenTime() == null) {
                return 1;
            }
            if (b.getHappenTime() == null) {
                return -1;
            }
            return b.getHappenTime().compareTo(a.getHappenTime());
        });
        return events;
    }


    private Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.valueOf(value.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(value.toString().trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String str(Object value) {
        return value == null ? null : value.toString();
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (value instanceof Date) {
            return new java.sql.Timestamp(((Date) value).getTime()).toLocalDateTime();
        }
        try {
            return LocalDateTime.parse(value.toString().replace(" ", "T"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 报告导出。
     * 补充 exportType 入参，异步生成文件并更新任务状态，
     * 任务与文件30分钟有效；原实现为占位空任务。
     */
    @Override
    public ReportExportResponse exportReport(String instanceId, String reportType, String exportType) {
        String taskId = "export_" + System.currentTimeMillis();
        ReportExportTaskHolder.ExportTask task = taskHolder.createTask(taskId);
        // 异步执行（ReportExportExecutor 为独立 Bean，保证 @Async 生效）
        reportExportExecutor.run(taskId, instanceId, reportType, exportType);

        ReportExportResponse resp = new ReportExportResponse();
        resp.setTaskId(taskId);
        resp.setTaskStatus(task.getTaskStatus());
        return resp;
    }

    /**
     * 查询导出任务状态。
     */
    @Override
    public ReportExportResponse queryExportTask(String taskId) {
        ReportExportTaskHolder.ExportTask task = taskHolder.getTask(taskId);
        if (task == null) {
            throw new BusinessException(BusinessCode.NOT_FOUND_ERROR.getCode(), "导出任务不存在或已过期");
        }
        ReportExportResponse resp = new ReportExportResponse();
        resp.setTaskId(task.getTaskId());
        resp.setTaskStatus(task.getTaskStatus());
        resp.setDownloadUrl(task.getDownloadUrl());
        resp.setFailReason(task.getFailReason());
        return resp;
    }

    /**
     * 获取导出文件。
     */
    @Override
    public File getExportFile(String taskId) {
        ReportExportTaskHolder.ExportTask task = taskHolder.getTask(taskId);
        if (task == null) {
            throw new BusinessException(BusinessCode.NOT_FOUND_ERROR.getCode(), "导出任务不存在或已过期");
        }
        if (!"success".equals(task.getTaskStatus()) || task.getFilePath() == null) {
            throw new BusinessException(BusinessCode.OPERATION_ERROR.getCode(), "导出任务未完成，无法下载");
        }
        File file = new File(task.getFilePath());
        if (!file.exists()) {
            throw new BusinessException(BusinessCode.OPERATION_ERROR.getCode(), "导出文件已过期，请重新导出");
        }
        return file;
    }

    /**
     * 取实例最新复制延迟（从 rms_metric_log 最新记录）。
     */
    private String getLatestReplDelay(String instanceId) {
        MetricLog latest = metricLogMapper.selectLatestByInstanceId(instanceId);
        return latest == null ? null : latest.getReplDelay();
    }

    /**
     * 根据告警规则判断复制状态。
     * - 无规则限制      -> 正常
     * - 有规则但未触发  -> 正常
     * - 有规则且触发    -> 延迟
     */
    private String judgeReplDelayStatus(String instanceId, String replDelay, List<AlarmRule> rules) {
        if (replDelay == null || replDelay.trim().isEmpty()) {
            return "正常";
        }
        double value;
        try {
            value = Double.parseDouble(replDelay.trim());
        } catch (Exception e) {
            return "正常";
        }

        boolean hasRule = false;
        for (AlarmRule rule : rules) {
            if (!REPL_DELAY_METRIC_KEY.equals(rule.getMetricKey())) {
                continue;
            }
            if (!ruleMatchHelper.isRuleApplyToInstance(rule, instanceId)) {
                continue;
            }
            hasRule = true;
            if (rule.getThreshold() == null) {
                continue;
            }
            double threshold;
            try {
                threshold = Double.parseDouble(rule.getThreshold());
            } catch (Exception e) {
                continue;
            }
            if (CompareUtil.compareValue(value, rule.getCompareType(), threshold)) {
                return "延迟";
            }
        }
        // 无规则 / 规则未触发 → 正常
        return hasRule ? "正常" : "正常";
    }



    /**
     * 判断实例是否在线：有心跳且不是"离线"
     */
    private boolean isOnline(RedisInstance inst) {
        if (inst.getLastHeartbeatTime() == null) {
            return false;
        }
        String desc = StatusEnum.descOf(inst.getRunStatus());
        return !StatusEnum.RUN_OFFLINE.getDesc().equals(desc);
    }
}