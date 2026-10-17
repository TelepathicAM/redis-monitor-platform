package com.danuvius.platform.model.dto.alarm.rule;

import lombok.Data;

import java.io.Serializable;

/** 状态切换请求 */
@Data
public class RuleToggleRequest implements Serializable {
    private String ruleId;
    private Boolean enableStatus;
}