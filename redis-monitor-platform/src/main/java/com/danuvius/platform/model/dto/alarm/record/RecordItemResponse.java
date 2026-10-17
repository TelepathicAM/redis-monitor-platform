package com.danuvius.platform.model.dto.alarm.record;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 告警记录查询
 */
@Data
public class RecordItemResponse implements Serializable {
    private String alarmNo;
    private String ruleId;
    private String instanceId;
    private String instanceName;
    private String hostIp;
    private String redisPort;
    private String metricName;
    private String alarmLevel;
    private String alarmContent;
    private String triggerValue;
    private String threshold;
    private String unit;
    private LocalDateTime triggerTime;
    private LocalDateTime recoverTime;
    private String recoverStatus;
}
