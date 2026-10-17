package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReportExportResponse implements Serializable {
    private String taskId;
    private String taskStatus;
    private String downloadUrl;
    private String failReason;
}