package com.danuvius.platform.controller;

import com.danuvius.platform.common.BaseResponse;
import com.danuvius.platform.common.ResultUtils;
import com.danuvius.platform.common.exception.BusinessCode;
import com.danuvius.platform.common.exception.validator.*;
import com.danuvius.platform.model.dto.alarm.record.*;
import com.danuvius.platform.model.dto.alarm.rule.*;
import com.danuvius.platform.service.AlarmRecordService;
import com.danuvius.platform.service.AlarmRuleService;
import com.danuvius.platform.util.validate.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/alarm")
public class AlarmController {

    private final AlarmRecordService alarmRecordService;
    private final AlarmRuleService alarmRuleService;

    public AlarmController(AlarmRecordService alarmRecordService, AlarmRuleService alarmRuleService) {
        this.alarmRecordService = alarmRecordService;
        this.alarmRuleService = alarmRuleService;
    }

    /**
     * 告警规则列表查询接口
     * @param req
     * @return
     */
    @GetMapping("/rule/list")
    public BaseResponse<RulePageResponse> getAlarmRuleList( RuleListRequest req) {
        RuleListRequestValidator.validate(req);
        log.info("告警规则列表查询接口：{}", req.toString());

        RulePageResponse rulePageResponse =alarmRuleService.getRulePage(req);
        return ResultUtils.success(rulePageResponse);
    }

    /**
     * 新增告警规则接口
     * @param req
     * @return 返回新增规则id
     */
    @PostMapping("/rule/add")
    public BaseResponse<String> addRule( @RequestBody RuleAddRequest req) {
        RuleAddRequestValidator.validate(req);
        log.info("新增告警规则接口：{}", req.toString());

        String ruleId = alarmRuleService.addRule(req);
        return ResultUtils.success(ruleId);
    }

    /**
     * 修改告警规则接口
     * @param req
     * @return
     */
    @PostMapping("/rule/update")
    public BaseResponse<Boolean> updateRule( @RequestBody UpdateRequest req) {
        UpdateRequestValidator.validate(req);
        log.info("修改告警规则接口：{}", req.toString());

        Boolean result=alarmRuleService.updateRule(req);
        if(result){
            return ResultUtils.success(result);
        }
        else return ResultUtils.error(BusinessCode.OPERATION_ERROR);
    }

    /**
     * 删除告警规则接口
     * @param ruleId
     * @return
     */
    @PostMapping("/rule/delete")
    public BaseResponse<Boolean> deleteRule( @RequestParam(required = false) String ruleId) {
        StringValidator.notBlank(ruleId, "ruleId");
        IdValidator.check(ruleId,"RL_",32,"ruleId");

        Boolean result=alarmRuleService.deleteRule(ruleId);
        if(result){
            return ResultUtils.success(result);
        }
        else return ResultUtils.error(BusinessCode.OPERATION_ERROR);
    }

    /**
     * 告警规则状态切换接口
     * @param req
     * @return
     */
    @PostMapping("/rule/toggle-status")
    public BaseResponse<Boolean> toggleStatus( @RequestBody RuleToggleRequest req) {
        RuleToggleRequestValidator.validate(req);
        log.info("告警规则状态切换接口：{}", req.toString());

        Boolean result=alarmRuleService.toggleStatus(req.getRuleId(), req.getEnableStatus());
        if(result){
            return ResultUtils.success(result);
        }
        else return ResultUtils.error(BusinessCode.OPERATION_ERROR);
    }

    /**
     * 单规则检测接口
     * @param ruleId
     * @return
     */
    @PostMapping("/rule/trigger-check")
    public BaseResponse<String> triggerCheck( @RequestParam(required = false) String ruleId) {
        StringValidator.notBlank(ruleId, "ruleId");
        IdValidator.check(ruleId,"RL_",32,"ruleId");

        return ResultUtils.success(alarmRuleService.triggerCheck(ruleId));
    }

    /**
     * 全规则检测接口
     * @return
     */
    @PostMapping("/rule/trigger-all-check")
    public BaseResponse<String> triggerAllCheck() {

        return ResultUtils.success(alarmRuleService.triggerAllCheck());
    }

    /**
     * 告警监控页概览统计接口
     * @return
     */
    @GetMapping("/rule/overview")
    public BaseResponse<RuleOverviewDTO> ruleOverview() {
        log.info("告警监控页概览统计接口");

        return ResultUtils.success(alarmRuleService.getRuleOverview());
    }

    /**
     * 未恢复告警列表接口
     * @return
     */
    @GetMapping("/rule/recent-unrecovered")
    public BaseResponse<List<RecordItemResponse>> recentUnrecovered() {
        log.info("未恢复告警列表接口");

        return ResultUtils.success(alarmRecordService.getRecentUnrecovered());
    }

    /**
     * 最近告警列表接口
     * @return
     */
    @GetMapping("/rule/recent")
    public BaseResponse<List<RecordItemResponse>> recent() {
        log.info("最近告警列表接口");

        return ResultUtils.success(alarmRecordService.getRecent());
    }

    /**
     * 告警记录页概览统计接口,最近3天
     * @return
     */
    @GetMapping("/record/overview")
    public BaseResponse<RecordOverviewResponse> recordOverview() {
        log.info("告警记录页概览统计接口，最近3天");

        return ResultUtils.success(alarmRecordService.getRecordOverview());
    }


    /**
     * 告警分页查询接口
     * @param req
     * @return
     */
    @GetMapping("/record/page")
    public BaseResponse<RecordPageResponse> recordPage( RecordPageRequest req) {
        RecordPageRequestValidator.validate(req);
        log.info("告警分页查询接口{}", req.toString());

        return ResultUtils.success(alarmRecordService.getRecordPage(req));
    }

    /**
     * 告警记录详情接口
     * @param alarmRecordId(对应表里的id)
     * @return
     */
    @GetMapping("/record/detail")
    public BaseResponse<RecordDetailResponse> recordDetail(@RequestParam String alarmRecordId) {
        IdValidator.snowflake(alarmRecordId, "alarmRecordId");
        log.info("告警记录详情接口{}", alarmRecordId);

        return ResultUtils.success(alarmRecordService.getRecordDetail(alarmRecordId));
    }

    /**
     * 告警关联指标波动曲线接口
     * @param alarmRecordId(对应表里的id)
     * @return
     */
    @GetMapping("/record/metric-trend")
    public BaseResponse<RecordMetricTrendResponse> metricTrend(@RequestParam  String alarmRecordId) {
        IdValidator.snowflake(alarmRecordId, "alarmRecordId");
        log.info("告警关联指标波动曲线接口{}", alarmRecordId);

        return ResultUtils.success(alarmRecordService.getMetricTrend(alarmRecordId));
    }

    /**
     * 告警人工确认恢复接口
     * @param alarmRecordId(对应表里的id)
     * @return
     */
    @PostMapping("/record/recover")
    public BaseResponse<Boolean> recoverRecord( @RequestParam String alarmRecordId) {
        IdValidator.snowflake(alarmRecordId, "alarmRecordId");
        log.info("告警人工确认恢复接口{}", alarmRecordId);

        Boolean result = alarmRecordService.recoverAlarm(alarmRecordId);
        if (result) {
            return ResultUtils.success(result);
        }
        else return ResultUtils.error(BusinessCode.OPERATION_ERROR);
    }

}