package com.danuvius.platform.controller;

import com.danuvius.platform.common.BaseResponse;
import com.danuvius.platform.common.ResultUtils;
import com.danuvius.platform.common.exception.validator.IdValidator;
import com.danuvius.platform.common.exception.validator.StringValidator;
import com.danuvius.platform.model.dto.metric.MetricReportItemDTO;
import com.danuvius.platform.model.entity.MetricLog;
import com.danuvius.platform.service.MetricLogService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/metric")
@Validated
public class MetricController {

    private final MetricLogService metricLogService;

    public MetricController(MetricLogService metricLogService) {
        this.metricLogService = metricLogService;
    }

    /**
     * Agent 上报指标。
     */
    @PostMapping("/report")
    public BaseResponse<String> report(@Valid @RequestBody MetricReportItemDTO req) {
        StringValidator.notBlank(req.getToken(), "token");

        metricLogService.reportMetric(req);
        return ResultUtils.success("OK");
    }

    /**
     * 查询某个 Agent 最新一条指标。
     */
    @GetMapping("/latest/{instanceId}")
    public BaseResponse<MetricLog> latest(
            @PathVariable @NotBlank(message = "instanceId不能为空") String instanceId) {
        IdValidator.snowflake(instanceId, "instanceId");

        return ResultUtils.success(metricLogService.getLatestByInstanceId(instanceId));
    }
}