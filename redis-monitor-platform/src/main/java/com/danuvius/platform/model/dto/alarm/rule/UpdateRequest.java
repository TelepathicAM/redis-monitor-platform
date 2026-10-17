package com.danuvius.platform.model.dto.alarm.rule;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 修改告警规则请求 */
@Data
public class UpdateRequest implements Serializable {
    private String ruleId;
    private String ruleName;
    private String metricKey;
    private String compareType;
    private Double threshold;
    private String unit;
    private String collectInterval;

    private String duration;
    private String alarmLevel;
    private String silenceDuration;
    private String scopeType;
    private List<String> scopeInstanceIds;
    private Boolean enableStatus;
}