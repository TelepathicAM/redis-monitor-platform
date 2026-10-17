package com.danuvius.platform.controller;

import com.danuvius.platform.common.BaseResponse;
import com.danuvius.platform.common.ResultUtils;
import com.danuvius.platform.common.exception.BusinessCode;
import com.danuvius.platform.model.dto.alarm.record.*;
import com.danuvius.platform.model.dto.alarm.rule.*;
import com.danuvius.platform.service.AlarmRecordService;
import com.danuvius.platform.service.AlarmRuleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/alarm")
@Validated
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
    public BaseResponse<RulePageResponse> getAlarmRuleList(@Valid RuleListRequest req) {
        RulePageResponse rulePageResponse =alarmRuleService.getRulePage(req);
        return ResultUtils.success(rulePageResponse);
    }

    /**
     * 新增告警规则接口
     * @param req
     * @return 返回新增规则id
     */
    @PostMapping("/rule/add")
    public BaseResponse<String> addRule(@Valid @RequestBody RuleAddRequest req) {
        String ruleId = alarmRuleService.addRule(req);
        return ResultUtils.success(ruleId);
    }

    /**
     * 修改告警规则接口
     * @param req
     * @return
     */
    @PostMapping("/rule/update")
    public BaseResponse<Boolean> updateRule(@Valid @RequestBody UpdateRequest req) {
        Boolean result=alarmRuleService.updateRule(req);
        if(result){
            return ResultUtils.success(result);
        }
        else return ResultUtils.error(BusinessCode.OPERATION_ERROR);
    }

    /**
     * 删除告警规则接口
     * @param req
     * @return
     */
    @PostMapping("/rule/delete")
    public BaseResponse<Boolean> deleteRule(@Valid @RequestBody RuleIdRequest req) {
        Boolean result=alarmRuleService.deleteRule(req.getRuleId());
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
    public BaseResponse<Boolean> toggleStatus(@Valid @RequestBody RuleToggleRequest req) {
        Boolean result=alarmRuleService.toggleStatus(req.getRuleId(), req.getEnableStatus());
        if(result){
            return ResultUtils.success(result);
        }
        else return ResultUtils.error(BusinessCode.OPERATION_ERROR);
    }

    /**
     * 单规则检测接口
     * @param req
     * @return
     */
    @PostMapping("/rule/trigger-check")
    public BaseResponse<String> triggerCheck(@Valid @RequestBody RuleIdRequest req) {
        return ResultUtils.success(alarmRuleService.triggerCheck(req.getRuleId()));
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
        return ResultUtils.success(alarmRuleService.getRuleOverview());
    }

    /**
     * 未恢复告警列表接口
     * @return
     */
    @GetMapping("/rule/recent-unrecovered")
    public BaseResponse<List<RecordItemResponse>> recentUnrecovered() {
        return ResultUtils.success(alarmRecordService.getRecentUnrecovered());
    }

    /**
     * 最近告警列表接口
     * @return
     */
    @GetMapping("/rule/recent")
    public BaseResponse<List<RecordItemResponse>> recent() {
        return ResultUtils.success(alarmRecordService.getRecent());
    }

    /**
     * 告警记录也概览统计接口
     * @param startTime
     * @param endTime
     * @return
     */
    @GetMapping("/record/overview")
    public BaseResponse<RecordOverviewResponse> recordOverview(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        return ResultUtils.success(alarmRecordService.getRecordOverview(startTime, endTime));
    }


    /**
     * 告警分页查询接口
     * @param req
     * @return
     */
    @GetMapping("/record/page")
    public BaseResponse<RecordPageResponse> recordPage(@Valid RecordPageRequest req) {
        return ResultUtils.success(alarmRecordService.getRecordPage(req));
    }

    /**
     * 告警记录详情接口
     * @param alarmRecordId
     * @return
     */
    @GetMapping("/record/detail")
    public BaseResponse<RecordDetailResponse> recordDetail(@RequestParam @NotBlank(message = "alarmRecordId不能为空") String alarmRecordId) {
        return ResultUtils.success(alarmRecordService.getRecordDetail(alarmRecordId));
    }

    /**
     * 告警关联指标波动曲线接口
     * @param alarmRecordId
     * @return
     */
    @GetMapping("/record/metric-trend")
    public BaseResponse<RecordMetricTrendResponse> metricTrend(
            @RequestParam @NotBlank(message = "alarmRecordId不能为空") String alarmRecordId) {
        return ResultUtils.success(alarmRecordService.getMetricTrend(alarmRecordId));
    }
}