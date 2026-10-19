package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReportExportResponse implements Serializable {
    /** 异步导出任务id，前端轮询用 */
    private String taskId;
    /** 任务状态：pending / success / fail */
    private String taskStatus;
    /** 文件临时下载链接 */
    private String downloadUrl;
    /** 失败原因，成功返回 null */
    private String failReason;
}