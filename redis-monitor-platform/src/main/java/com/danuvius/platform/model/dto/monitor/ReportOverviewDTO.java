package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 日周报概览统计。
 * 字段与原方案 /api/monitor/report/query 接口返回的 overview 定义一致。
 */
@Data
public class ReportOverviewDTO implements Serializable {

    private Double cpuAvg;
    private Double cpuMax;
    private LocalDateTime cpuMaxTim;

    private Double redisMemAvg;
    private Double redisMemMax;
    private LocalDateTime redisMemMaxTim;

    private Double qpsAvg;
    private Double qpsMax;
    private LocalDateTime qpsMaxTim;

    private Double latencyAvg;
    private Double latencyMax;
    private LocalDateTime latencyMaxTim;

    /** 告警总次数 */
    private Long alarmTotal;

    /** 告警分级统计 */
    private AlarmSeverityCountDTO alarmSeverityCount;

    /** 主从切换总次数 */
    private Long switchTotal;

    /** 最近切换时间，无则返回 null */
    private LocalDateTime lastSwitchTime;

    /** Redis服务可用率百分比（节点可用率） */
    private BigDecimal serviceAvailableRate;

    /** 主节点数 */
    private Long masterCount;

    /** 从节点数 */
    private Long replicaCount;

    /** 告警分级统计 {提示:xx, 警告:xx, 严重:xx} */
    @Data
    public static class AlarmSeverityCountDTO implements Serializable {
        /** 提示级别告警数 */
        private Long info;
        /** 警告级别告警数 */
        private Long alarm;
        /** 严重级别告警数 */
        private Long severity;
    }
}
