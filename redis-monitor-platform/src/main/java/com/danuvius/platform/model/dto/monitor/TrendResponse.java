package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TrendResponse implements Serializable {
    /** 指标名称 */
    private String metricName;

    /** 指标单位 */
    private String unit;
    /** 统一x轴时间戳数组 */
    private List<String> timestampList;
    /** 节点曲线数组 */
    private List<TrendSeriesDTO> seriesList;
}