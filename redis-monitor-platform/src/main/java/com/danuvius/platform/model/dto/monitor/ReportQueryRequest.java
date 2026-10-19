package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReportQueryRequest implements Serializable {
    /*实例id*/
    private String instanceId;
    /*报告类型：日报 / 周报*/
    private String reportType;
}