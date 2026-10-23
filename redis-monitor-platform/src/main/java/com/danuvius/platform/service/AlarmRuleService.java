package com.danuvius.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.danuvius.platform.model.dto.alarm.rule.*;
import com.danuvius.platform.model.entity.AlarmRule;

public interface AlarmRuleService extends IService<AlarmRule> {
    /**
     * 规则列表查询
     * @param ruleListRequest
     * @return
     */
    RulePageResponse getRulePage(RuleListRequest ruleListRequest);

    /**
     * 新增规则
     * @param alarmRuleAddRequest
     * @return 新增规则id
     */
    String addRule(RuleAddRequest alarmRuleAddRequest);

    /**
     * 更新规则
     * @param updateRequest
     * @return
     */
    boolean updateRule(UpdateRequest updateRequest);

    /**
     * 规则逻辑删除
     * @param ruleId
     * @return
     */
    boolean deleteRule(String ruleId);

    /**
     * 规则启用状态更新
     * @param ruleId
     * @param enableStatus
     * @return
     */
    boolean toggleStatus(String ruleId, String enableStatus);

    /**
     * 单规则状态查询
     * @param ruleId
     * @return
     */
    String triggerCheck(String ruleId);

    /**
     * 全规则状态查询
     * @return
     */
    String triggerAllCheck();

    /**
     * 单规则检测执行体，异步调用
     */
    void executeSingleCheck(String ruleId);

    /**
     * 全规则检测执行体，异步调用
     */
    void executeAllCheck();

    /**
     * 规则概览
     * @return
     */
    RuleOverviewDTO getRuleOverview();

}
