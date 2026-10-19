package com.danuvius.platform.model.dto.alarm.rule;

import lombok.Data;

import java.io.Serializable;

/** 告警规则概览统计 */
@Data
public class RuleOverviewDTO implements Serializable {
    /** 已启用告警规则数量 */
    private Long enableRuleCount;
    /** 当前未恢复告警总数 */
    private Long unrecoveredAlarmCount;
    /** 今日告警总数 */
    private Long todayAlarmTotal;

    /* 案补今日告警分级统计 {提示:xx, 警告:xx, 严重:xx} */
    /** 今日告警分级统计 */
    private TodayAlarmLevelCount todayAlarmLevelCount;

    /* 告警检测任务状态：运行正常 / 运行异常 */
    /** 告警检测任务状态：运行正常 / 运行异常 */
    private String detectTaskStatus;

    /* 承接SQL分级统计结果的扁平字段，service 中组装为 todayAlarmLevelCount */
    /** 今日提示级别告警数（SQL 扁平字段） */
    private Long todayInfoCount;
    /** 今日警告级别告警数（SQL 扁平字段） */
    private Long todayAlarmCount;
    /** 今日严重级别告警数（SQL 扁平字段） */
    private Long todaySeverityCount;

    /** 今日告警分级统计 */
    @Data
    public static class TodayAlarmLevelCount implements Serializable {
        /** 提示级别告警数 */
        private Long info;
        /** 警告级别告警数 */
        private Long alarm;
        /** 严重级别告警数 */
        private Long severity;
    }
}