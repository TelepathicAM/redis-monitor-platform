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
    /*目标启用状态：1= 启用 / 0 禁用*/
    private String enableStatus;
}