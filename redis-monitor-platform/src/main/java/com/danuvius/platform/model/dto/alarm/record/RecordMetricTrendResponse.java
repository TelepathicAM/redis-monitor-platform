package com.danuvius.platform.model.dto.alarm.record;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 告警关联指标波动曲线
 */
@Data
public class RecordMetricTrendResponse implements Serializable {
    /*指标中文名称*/
    private String metricName;
    /*指标单位：%、GB、ms、KB/s 等*/
    private String unit;
    /*告警阈值数值，用于绘制阈值水平线*/
    private String thresholdValue;
    /*告警触发时间戳，用于前端竖线标记*/
    private LocalDateTime triggerTime;
    /*告警恢复时间戳；未恢复返回 null*/
    private LocalDateTime recoverTime;
    /*查询时间段全部时间戳数组*/
    private List<String> timestampList;
    /*对应时间点指标值，无采集点返回 null，前端断点*/
    private List<Double> valueList;
}