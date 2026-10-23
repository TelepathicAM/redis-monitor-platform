package com.danuvius.platform.model.dto.alarm.rule;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 新增告警规则请求
 */
@Data
public class RuleAddRequest implements Serializable {
    /*告警规则名称*/
    private String ruleName;
    /*监控指标 key，如 cpuUsage、qps*/
    private String metricKey;
    /*比较方式：>、<、=、>=、<=*/
    private String compareType;
    /*告警阈值*/
    private Double threshold;
    /*阈值单位*/
    private String unit;
    /*采集间隔，单位秒，默认 300*/
    private String collectInterval;
    /*持续触发时长，单位个采集周期，默认 1*/
    private String duration;
    /*告警级别：1 提示 / 2 警告 / 3 严重*/
    private String alarmLevel;
    /*告警静默期，单位秒，默认 1800（30min）*/
    private String silenceDuration;
    /*生效范围：1 全部节点 / 2 指定节点*/
    private String scopeType;
    /*生效实例 id 列表，scopeType=2 时有效*/
    private List<String> scopeInstanceIds;
    /*启用状态：true 启用 / false 禁用*/
    private String enableStatus;
    /*创建人*/
    private String createdBy;
}