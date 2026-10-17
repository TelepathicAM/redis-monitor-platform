package com.danuvius.platform.model.dto.alarm.rule;

import lombok.Data;

import java.io.Serializable;

/** 仅含 ruleId 的返回（删除、手动检测等） */
@Data
public class RuleIdRequest implements Serializable {
    private String ruleId;
}