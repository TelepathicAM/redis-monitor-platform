package com.danuvius.platform.model.dto.alarm.rule;

import lombok.Data;

import java.io.Serializable;

/** 告警规则概览统计 */
@Data
public class RuleOverviewDTO implements Serializable {
    private Long enableRuleCount;
    private Long unrecoveredAlarmCount;
    private Long todayAlarmTotal;

    /* 案补今日告警分级统计 {提示:xx, 警告:xx, 严重:xx} */
    private TodayAlarmLevelCount todayAlarmLevelCount;

    /* 告警检测任务状态：运行正常 / 运行异常 */
    private String detectTaskStatus;

    /* 承接SQL分级统计结果的扁平字段，service 中组装为 todayAlarmLevelCount */
    private Long todayInfoCount;
    private Long todayAlarmCount;
    private Long todaySeverityCount;

    /** 今日告警分级统计 */
    @Data
    public static class TodayAlarmLevelCount implements Serializable {
        private Long info;
        private Long alarm;
        private Long severity;
    }
}