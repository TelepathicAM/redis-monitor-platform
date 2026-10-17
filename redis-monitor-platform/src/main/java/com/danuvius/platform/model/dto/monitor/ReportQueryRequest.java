package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReportQueryRequest implements Serializable {
    private String instanceId;
    private String reportType;
}