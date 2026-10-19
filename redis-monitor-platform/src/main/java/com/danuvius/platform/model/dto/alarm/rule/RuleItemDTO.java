package com.danuvius.platform.model.dto.alarm.rule;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警规则列表单项
 */
@Data
public class RuleItemDTO implements Serializable {
    /*告警规则id*/
    private String ruleId;
    /*告警规则名称*/
    private String ruleName;
    /*监控指标 key*/
    private String metricKey;
    /*比较方式：>、<、=、>=、<=*/
    private String compareType;
    /*告警阈值*/
    private String threshold;
    /*阈值单位*/
    private String unit;
    /*采集间隔，单位秒*/
    private String collectInterval;
    /*持续触发时长*/
    private String duration;
    /*告警级别：1 提示 / 2 警告 / 3 严重*/
    private String alarmLevel;
    /*告警静默期，单位秒*/
    private String silenceDuration;
    /*生效范围：1 全部节点 / 2 指定节点*/
    private String scopeType;

    /* 生效实例ID数组（scopeType=2 有效）*/
    /*生效实例id串，逗号分隔*/
    private String scopeInstanceId;

    /* 生效实例名称展示数组，前端直接展示*/
    /*生效实例名称展示数组*/
    private List<String> scopeInstanceNames;

    /*启用状态：0 禁用 / 1 启用*/
    private String enableStatus;
    /*创建时间*/
    private LocalDateTime createdTime;
    /*最后更新时间*/
    private LocalDateTime updatedTime;
}