package com.danuvius.platform.controller;

import com.danuvius.platform.common.BaseResponse;
import com.danuvius.platform.common.ResultUtils;
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
        return ResultUtils.success(redisInstanceService.getCurrentValues(instanceId));
    }

    /**
     * 多节点指标趋势接口
     * @param req
     * @return
     */
    @GetMapping("/realtime/trend")
    public BaseResponse<TrendResponse> trend(@Valid TrendRequest req) {
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
        return ResultUtils.success(redisInstanceService.queryReport(req));
    }


    /**
     * Todo 去除exportType
     */

    /**
     * 报告导出接口
     * @param instanceId
     * @param reportType
     * @param exportType
     * @return
     */
    @GetMapping("/report/export")
    public BaseResponse<ReportExportResponse> reportExport(
            @RequestParam @NotBlank(message = "instanceId不能为空") String instanceId,
            @RequestParam @NotBlank(message = "reportType不能为空") String reportType,
            @RequestParam(defaultValue = "excel") String exportType) {
        return ResultUtils.success(
                redisInstanceService.exportReport(instanceId, reportType, exportType));
    }

    /**
     * 导出任务状态查询接口，前端3s轮询
     */
    @GetMapping("/report/export/status")
    public BaseResponse<ReportExportResponse> exportStatus(
            @RequestParam @NotBlank(message = "taskId不能为空") String taskId) {
        return ResultUtils.success(redisInstanceService.queryExportTask(taskId));
    }

    /**
     * 导出文件下载接口,任务成功后返回临时文件，30分钟有效
     */
    @GetMapping("/report/export/download")
    public void exportDownload(
            @RequestParam @NotBlank(message = "taskId不能为空") String taskId,
            javax.servlet.http.HttpServletResponse response) throws java.io.IOException {
        java.io.File file = redisInstanceService.getExportFile(taskId);
        String fileName = java.net.URLEncoder.encode(file.getName(), "UTF-8").replace("+", "%20");
        String contentType = file.getName().endsWith(".csv")
                ? "text/csv;charset=UTF-8" : "text/html;charset=UTF-8";
        response.setContentType(contentType);
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