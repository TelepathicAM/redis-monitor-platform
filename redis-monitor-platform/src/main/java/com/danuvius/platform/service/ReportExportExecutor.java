package com.danuvius.platform.service;

import com.danuvius.platform.model.dto.monitor.ReportQueryResponse;
import com.danuvius.platform.util.ReportExportTaskHolder;
import com.danuvius.platform.util.ReportFileGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 报告导出异步执行器。
 * 独立成 Bean 是为了让 @Async 生效（Spring 自调用不走代理）。
 * 对应原方案"异步提交报告生成任务…文件生成后更新任务状态为成功，关联文件下载链接；
 * 若文件生成失败则更新任务状态为失败，返回失败原因"。
 */
@Slf4j
@Component
public class ReportExportExecutor {

    private final RedisInstanceService redisInstanceService;
    private final ReportFileGenerator reportFileGenerator;
    private final ReportExportTaskHolder taskHolder;
    private final AuditLogService auditLogService;

    public ReportExportExecutor(RedisInstanceService redisInstanceService,
                                ReportFileGenerator reportFileGenerator,
                                ReportExportTaskHolder taskHolder,
                                AuditLogService auditLogService) {
        this.redisInstanceService = redisInstanceService;
        this.reportFileGenerator = reportFileGenerator;
        this.taskHolder = taskHolder;
        this.auditLogService = auditLogService;
    }

    /**
     * 异步执行报告导出。
     */
    @Async("taskExecutor")
    public void run(String taskId, String instanceId, String reportType, String exportType) {
        ReportExportTaskHolder.ExportTask task = taskHolder.getTask(taskId);
        if (task == null) {
            return;
        }
        try {
            log.info("[报告导出] 异步任务开始, taskId={}, instanceId={}, reportType={}, exportType={}",
                    taskId, instanceId, reportType, exportType);

            // 复用报告查询逻辑获取完整报告数据
            ReportQueryResponse report = redisInstanceService.buildReportData(instanceId, reportType);
            File file = reportFileGenerator.generate(exportType, report, instanceId, reportType,
                    taskHolder.exportDir());

            task.setFilePath(file.getAbsolutePath());
            task.setFileName(file.getName());
            task.setDownloadUrl("/api/monitor/report/export/download?taskId=" + taskId);
            task.setTaskStatus("success");

            /* 将操作人、文件id、报告类型、导出实例、时间范围、操作类型、操作时间记入操作审计日志 */
            auditLogService.record("报告导出", "导出", taskId,
                    "导出Redis运行报告，实例：" + instanceId + "，报告类型：" + reportType
                            + "，导出格式：" + exportType);

            log.info("[报告导出] 异步任务完成, taskId={}, file={}", taskId, file.getAbsolutePath());
        } catch (Exception e) {
            task.setTaskStatus("fail");
            task.setFailReason(e.getMessage());
            log.error("[报告导出] 异步任务失败, taskId={}", taskId, e);
        }
    }
}
