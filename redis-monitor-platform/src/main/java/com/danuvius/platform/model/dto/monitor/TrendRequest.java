package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TrendRequest implements Serializable {
    private List<String> instanceIds;
    private String metricKey;
    private String timeRange;
}