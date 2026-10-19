package com.danuvius.platform.controller;

import com.danuvius.platform.common.BaseResponse;
import com.danuvius.platform.common.ResultUtils;
import com.danuvius.platform.common.exception.BusinessCode;
import com.danuvius.platform.common.exception.validator.*;
import com.danuvius.platform.model.dto.alarm.record.*;
import com.danuvius.platform.model.dto.alarm.rule.*;
import com.danuvius.platform.service.AlarmRecordService;
import com.danuvius.platform.service.AlarmRuleService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/alarm")
@Validated
public class AlarmController {
    /** [新增校验] 告警级别合法值 */
    private static final String[] ALARM_LEVELS = {"1", "2", "3"};
    /** [新增校验] 比较方式合法值 */
    private static final String[] COMPARE_TYPES = {">", "<", ">=", "<=", "="};
    /** [新增校验] 生效范围合法值 */
    private static final String[] SCOPE_TYPES = {"1", "2"};
    /** [新增校验] 启用状态合法值 */
    private static final String[] ENABLE_STATUS = {"0", "1"};
    /** [新增校验] 恢复状态合法值 */
    private static final String[] RECOVER_STATUS = {"0", "1"};

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
        NumberValidator.between(req.getPageNum(), 1, 10000, "pageNum");
        NumberValidator.between(req.getPageSize(), 1, 100, "pageSize");
        if (StringUtils.isNotBlank(req.getAlarmLevel())) {
            ParameterValidator.in(req.getAlarmLevel(), ALARM_LEVELS, "alarmLevel");
        }
        if (StringUtils.isNotBlank(req.getEnableStatus())) {
            ParameterValidator.in(req.getEnableStatus(), ENABLE_STATUS, "enableStatus");
        }
        if (req.getScopeInstanceIds() != null) {
            CollectionValidator.maxSize(req.getScopeInstanceIds(), 100, "scopeInstanceIds");
            CollectionValidator.noBlankStringElement(req.getScopeInstanceIds(), "scopeInstanceIds");
        }
        if (StringUtils.isNotBlank(req.getKeyword())) {
            StringValidator.maxLength(req.getKeyword(), 64, "keyword");
        }
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
        StringValidator.notBlank(req.getRuleName(), "ruleName");
        StringValidator.maxLength(req.getRuleName(), 64, "ruleName");
        StringValidator.notBlank(req.getMetricKey(), "metricKey");
        StringValidator.maxLength(req.getMetricKey(), 64, "metricKey");
        ParameterValidator.in(req.getCompareType(), COMPARE_TYPES, "compareType");
        NumberValidator.notNull(req.getThreshold(), "threshold");
        ParameterValidator.in(req.getAlarmLevel(), ALARM_LEVELS, "alarmLevel");
        ParameterValidator.in(req.getScopeType(), SCOPE_TYPES, "scopeType");
        // scopeType=2 时必须指定生效实例
        if ("2".equals(req.getScopeType())) {
            CollectionValidator.notEmpty(req.getScopeInstanceIds(), "scopeInstanceIds");
            CollectionValidator.maxSize(req.getScopeInstanceIds(), 100, "scopeInstanceIds");
            CollectionValidator.noBlankStringElement(req.getScopeInstanceIds(), "scopeInstanceIds");
        }
        // 数字字符串字段：非空则必须为整数
        NumberValidator.parseInteger(req.getCollectInterval(), "collectInterval");
        NumberValidator.parseInteger(req.getDuration(), "duration");
        NumberValidator.parseInteger(req.getSilenceDuration(), "silenceDuration");

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
        StringValidator.notBlank(req.getRuleId(), "ruleId");
        StringValidator.notBlank(req.getRuleName(), "ruleName");
        StringValidator.maxLength(req.getRuleName(), 64, "ruleName");
        StringValidator.notBlank(req.getMetricKey(), "metricKey");
        StringValidator.maxLength(req.getMetricKey(), 64, "metricKey");
        ParameterValidator.in(req.getCompareType(), COMPARE_TYPES, "compareType");
        NumberValidator.notNull(req.getThreshold(), "threshold");
        ParameterValidator.in(req.getAlarmLevel(), ALARM_LEVELS, "alarmLevel");
        ParameterValidator.in(req.getScopeType(), SCOPE_TYPES, "scopeType");
        if ("2".equals(req.getScopeType())) {
            CollectionValidator.notEmpty(req.getScopeInstanceIds(), "scopeInstanceIds");
            CollectionValidator.maxSize(req.getScopeInstanceIds(), 100, "scopeInstanceIds");
            CollectionValidator.noBlankStringElement(req.getScopeInstanceIds(), "scopeInstanceIds");
        }
        NumberValidator.notNull(req.getEnableStatus() == null ? null : 1, "enableStatus");
        NumberValidator.parseInteger(req.getCollectInterval(), "collectInterval");
        NumberValidator.parseInteger(req.getDuration(), "duration");
        NumberValidator.parseInteger(req.getSilenceDuration(), "silenceDuration");

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
        StringValidator.notBlank(req.getRuleId(), "ruleId");

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
        StringValidator.notBlank(req.getRuleId(), "ruleId");
        NumberValidator.notNull(req.getEnableStatus() == null ? null : 1, "enableStatus");

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
        StringValidator.notBlank(req.getRuleId(), "ruleId");

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
     * 告警记录页概览统计接口
     * @param startTime
     * @param endTime
     * @return
     */
    @GetMapping("/record/overview")
    public BaseResponse<RecordOverviewResponse> recordOverview(
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        DateValidator.stringRange(startTime, endTime, "startTime", "endTime");

        return ResultUtils.success(alarmRecordService.getRecordOverview(startTime, endTime));
    }


    /**
     * 告警分页查询接口
     * @param req
     * @return
     */
    @GetMapping("/record/page")
    public BaseResponse<RecordPageResponse> recordPage(@Valid RecordPageRequest req) {
        NumberValidator.between(req.getPageNum(), 1, 10000, "pageNum");
        NumberValidator.between(req.getPageSize(), 1, 100, "pageSize");
        if (StringUtils.isNotBlank(req.getAlarmLevel())) {
            ParameterValidator.in(req.getAlarmLevel(), ALARM_LEVELS, "alarmLevel");
        }
        if (StringUtils.isNotBlank(req.getRecoverStatus())) {
            ParameterValidator.in(req.getRecoverStatus(), RECOVER_STATUS, "recoverStatus");
        }
        if (StringUtils.isNotBlank(req.getInstanceId())) {
            IdValidator.snowflake(req.getInstanceId(), "instanceId");
        }
        if (StringUtils.isNotBlank(req.getMetricKey())) {
            StringValidator.maxLength(req.getMetricKey(), 64, "metricKey");
        }
        if (StringUtils.isNotBlank(req.getKeyword())) {
            StringValidator.maxLength(req.getKeyword(), 128, "keyword");
        }
        DateValidator.stringRange(req.getStartTime(), req.getEndTime(), "startTime", "endTime");


        return ResultUtils.success(alarmRecordService.getRecordPage(req));
    }

    /**
     * 告警记录详情接口
     * @param alarmRecordId
     * @return
     */
    @GetMapping("/record/detail")
    public BaseResponse<RecordDetailResponse> recordDetail(@RequestParam String alarmRecordId) {
        IdValidator.snowflake(alarmRecordId, "alarmRecordId");

        return ResultUtils.success(alarmRecordService.getRecordDetail(alarmRecordId));
    }

    /**
     * 告警关联指标波动曲线接口
     * @param alarmRecordId
     * @return
     */
    @GetMapping("/record/metric-trend")
    public BaseResponse<RecordMetricTrendResponse> metricTrend(
            @RequestParam  String alarmRecordId) {
        IdValidator.snowflake(alarmRecordId, "alarmRecordId");

        return ResultUtils.success(alarmRecordService.getMetricTrend(alarmRecordId));
    }

    /**
     * 告警人工确认恢复接口
     * @param alarmRecordId
     * @return
     */
    @PostMapping("/record/recover")
    public BaseResponse<Boolean> recoverRecord(@Valid @RequestParam String alarmRecordId) {
        IdValidator.snowflake(alarmRecordId, "alarmRecordId");

        Boolean result = alarmRecordService.recoverAlarm(alarmRecordId);
        if (result) {
            return ResultUtils.success(result);
        }
        else return ResultUtils.error(BusinessCode.OPERATION_ERROR);
    }

}