package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TrendRequest implements Serializable {
    /** 实例id列表 */
    private List<String> instanceIds;
    /** 指标 key */
    private String metricKey;
    /** 时间范围：近1h / 近6h */
    private String timeRange;
}