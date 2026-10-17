package com.danuvius.platform.model.dto.alarm.rule;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/** 告警规则列表单项 */
@Data
public class RuleItemDTO implements Serializable {
    private String ruleId;
    private String ruleName;
    private String metricKey;
    private String compareType;
    private String threshold;
    private String unit;
    private String collectInterval;
    private String duration;
    private String alarmLevel;
    private String silenceDuration;
    private String scopeType;

    /* 生效实例ID数组（scopeType=2 有效） */
    private String scopeInstanceId;

    /* 生效实例名称展示数组，前端直接展示 */
    private List<String> scopeInstanceNames;


    private String enableStatus;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}