package com.danuvius.platform.model.dto.alarm.rule;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.danuvius.platform.model.entity.AlarmRule;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 告警规则分页响应
 */
@Data
public class RulePageResponse implements Serializable {
    /* 生效实例名称展示数组，前端直接展示*/
    /*生效实例名称展示数组*/
    private List<String> scopeInstanceNames;

    /*当前分页结果*/
    private Page<AlarmRule> page;
}