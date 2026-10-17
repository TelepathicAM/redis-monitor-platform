package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 日周报单项指标趋势序列 */
@Data
public class ReportTrendDTO implements Serializable {
    private String metricKey;
    private String metricName;
    private String unit;
    private List<String> timestampList;
    private List<Double> valueList;
}
