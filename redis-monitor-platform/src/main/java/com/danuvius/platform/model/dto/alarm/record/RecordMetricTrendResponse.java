package com.danuvius.platform.model.dto.alarm.record;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/** 告警关联指标波动曲线 */
@Data
public class RecordMetricTrendResponse implements Serializable {
    private String metricName;
    private String thresholdValue;
    private LocalDateTime triggerTime;
    private LocalDateTime recoverTime;
    private List<String> timestampList;
    private List<Double> valueList;
}