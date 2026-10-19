package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 日周报单项指标趋势序列 */
@Data
public class ReportTrendDTO implements Serializable {
    /** 指标 key */
    private String metricKey;
    /** 指标中文名称 */
    private String metricName;
    /** 指标单位 */
    private String unit;
    /** 时间戳数组 */
    private List<String> timestampList;
    /** 时间戳对应指标值数组，缺失值为 null */
    private List<Double> valueList;
}