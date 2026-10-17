package com.danuvius.platform.service;

import com.danuvius.platform.util.DetectTaskRecorder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 告警检测异步执行器。
 * 独立成 Bean 保证 @Async 生效（Spring 同类自调用不走代理）。
 * 对应原方案"异步提交检测任务…同步返回提示，不等待检测完成"。
 */
@Slf4j
@Component
public class AlarmCheckExecutor {

    private final AlarmRuleService alarmRuleService;
    private final DetectTaskRecorder detectTaskRecorder;

    public AlarmCheckExecutor(AlarmRuleService alarmRuleService, DetectTaskRecorder detectTaskRecorder) {
        this.alarmRuleService = alarmRuleService;
        this.detectTaskRecorder = detectTaskRecorder;
    }

    /**
     * 异步执行单规则检测。
     */
    @Async("taskExecutor")
    public void runSingle(String ruleId) {
        try {
            alarmRuleService.executeSingleCheck(ruleId);
            detectTaskRecorder.record("single-rule", true);
        } catch (Exception e) {
            log.error("[手动检测] 单规则检测任务执行失败, ruleId={}", ruleId, e);
            detectTaskRecorder.record("single-rule", false);
        }
    }

    /**
     * 异步执行全量规则检测。
     */
    @Async("taskExecutor")
    public void runAll() {
        try {
            alarmRuleService.executeAllCheck();
            detectTaskRecorder.record("all-rule", true);
        } catch (Exception e) {
            log.error("[手动检测] 全量检测任务执行失败", e);
            detectTaskRecorder.record("all-rule", false);
        }
    }
}
