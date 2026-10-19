package com.danuvius.platform.model.dto.alarm.rule;

import lombok.Data;

import java.io.Serializable;

/**
 * 状态切换请求
 */
@Data
public class RuleToggleRequest implements Serializable {
    /*告警规则id*/
    private String ruleId;
    /*目标启用状态：true 启用 / false 禁用*/
    private Boolean enableStatus;
}