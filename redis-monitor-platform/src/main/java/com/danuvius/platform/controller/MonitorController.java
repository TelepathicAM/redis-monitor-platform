package com.danuvius.platform.controller;

import com.danuvius.platform.common.BaseResponse;
import com.danuvius.platform.common.ResultUtils;
import com.danuvius.platform.common.exception.validator.CollectionValidator;
import com.danuvius.platform.common.exception.validator.IdValidator;
import com.danuvius.platform.common.exception.validator.ParameterValidator;
import com.danuvius.platform.common.exception.validator.StringValidator;
import com.danuvius.platform.model.dto.monitor.*;
import com.danuvius.platform.service.RedisInstanceService;
import com.danuvius.platform.service.SentinelNodeInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/monitor")
@Slf4j
@Validated
public class MonitorController {
    /** [新增校验] 纳管状态合法值 */
    private static final String[] MANAGEMENT_STATUS = {"0", "1"};
    /** [新增校验] 趋势时间范围合法值 */
    private static final String[] TIME_RANGES = {"近1h", "近6h"};
    /** [新增校验] 报告类型合法值 */
    private static final String[] REPORT_TYPES = {"日报", "周报"};
    /** [新增校验] 趋势指标 key 合法值（与 MetricTypeEnum 采集指标对齐） */
    private static final String[] TREND_METRIC_KEYS = {
            "cpuUsage", "serverMemUsage", "redisMemUsed", "qps", "avgLatency",
            "netInputBandwidth", "netOutputBandwidth"
    };


    private final RedisInstanceService redisInstanceService;
    private final SentinelNodeInfoService sentinelNodeInfoService;

    public MonitorController(RedisInstanceService redisInstanceService, SentinelNodeInfoService sentinelNodeInfoService) {
        this.redisInstanceService = redisInstanceService;
        this.sentinelNodeInfoService = sentinelNodeInfoService;
    }

    /**
     * 查询实例列表接口
     * @param managementStatus
     * @return
     */
    @GetMapping("/redis/instances")
    public BaseResponse<List<RedisInstanceItemDTO>> instances(
            @RequestParam(defaultValue = "1") String managementStatus) {
        ParameterValidator.in(managementStatus, MANAGEMENT_STATUS, "managementStatus");

        return ResultUtils.success(redisInstanceService.getInstanceList(managementStatus));
    }

    /**
     * 核心实时指标接口
     * @param instanceId
     * @return
     */
    @GetMapping("/realtime/current-values")
    public BaseResponse<MetricCurrentValueResponse> currentValues(
            @RequestParam @NotBlank(message = "instanceId不能为空") String instanceId) {
        IdValidator.snowflake(instanceId, "instanceId");

        return ResultUtils.success(redisInstanceService.getCurrentValues(instanceId));
    }

    /**
     * 多节点指标趋势接口
     * @param req
     * @return
     */
    @GetMapping("/realtime/trend")
    public BaseResponse<TrendResponse> trend(@Valid TrendRequest req) {
        CollectionValidator.notEmpty(req.getInstanceIds(), "instanceIds");
        CollectionValidator.maxSize(req.getInstanceIds(), 50, "instanceIds");
        CollectionValidator.noBlankStringElement(req.getInstanceIds(), "instanceIds");
        StringValidator.notBlank(req.getMetricKey(), "metricKey");
        ParameterValidator.in(req.getMetricKey(), TREND_METRIC_KEYS, "metricKey");
        StringValidator.notBlank(req.getTimeRange(), "timeRange");
        ParameterValidator.in(req.getTimeRange(), TIME_RANGES, "timeRange");

        return ResultUtils.success(redisInstanceService.getTrend(req));
    }

    /**
     * Sentinel节点列表接口
     * @param managementStatus
     * @return
     */
    @GetMapping("/sentinel/sentinels")
    public BaseResponse<List<SentinelItemResponse>> sentinels(
            @RequestParam(defaultValue = "1") String managementStatus) {
        ParameterValidator.in(managementStatus, MANAGEMENT_STATUS, "managementStatus");

        return ResultUtils.success(sentinelNodeInfoService.getSentinelList(managementStatus));
    }

    /**
     * 主从拓扑接口
     * @return
     */
    @GetMapping("/sentinel/topology")
    public BaseResponse<TopologyResponse> topology() {
        return ResultUtils.success(redisInstanceService.getTopology());
    }

    /**
     * 主从状态表格接口
     * @return
     */
    @GetMapping("/sentinel/nodes-status")
    public BaseResponse<List<NodeStatusResponse>> nodesStatus() {
        return ResultUtils.success(redisInstanceService.getNodesStatus());
    }

    /**
     * 日周报查询接口
     * @param req
     * @return
     */
    @PostMapping("/report/query")
    public BaseResponse<ReportQueryResponse> reportQuery(@Valid @RequestBody ReportQueryRequest req) {
        IdValidator.snowflake(req.getInstanceId(), "instanceId");
        StringValidator.notBlank(req.getReportType(), "reportType");
        ParameterValidator.in(req.getReportType(), REPORT_TYPES, "reportType");

        return ResultUtils.success(redisInstanceService.queryReport(req));
    }



    /**
     * 报告导出接口
     * @param instanceId
     * @param reportType
     * @return
     */
    @GetMapping("/report/export")
    public BaseResponse<ReportExportResponse> reportExport(
            @RequestParam @NotBlank(message = "instanceId不能为空") String instanceId,
            @RequestParam @NotBlank(message = "reportType不能为空") String reportType) {
        IdValidator.snowflake(instanceId, "instanceId");
        ParameterValidator.in(reportType, REPORT_TYPES, "reportType");

        return ResultUtils.success(
                redisInstanceService.exportReport(instanceId, reportType));
    }

    /**
     * 导出任务状态查询接口，前端3s轮询
     */
    @GetMapping("/report/export/status")
    public BaseResponse<ReportExportResponse> exportStatus(
            @RequestParam @NotBlank(message = "taskId不能为空") String taskId) {
        IdValidator.prefixed(taskId, "taskId");

        return ResultUtils.success(redisInstanceService.queryExportTask(taskId));
    }

    /**
     * 导出文件下载接口,任务成功后返回临时文件，30分钟有效
     */
    @GetMapping("/report/export/download")
    public void exportDownload(
            @RequestParam @NotBlank(message = "taskId不能为空") String taskId,
            javax.servlet.http.HttpServletResponse response) throws java.io.IOException {
        IdValidator.prefixed(taskId, "taskId");

        java.io.File file = redisInstanceService.getExportFile(taskId);
        String fileName = java.net.URLEncoder.encode(file.getName(), "UTF-8").replace("+", "%20");

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);
        try (java.io.InputStream in = java.nio.file.Files.newInputStream(file.toPath());
             java.io.OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        }
    }
}