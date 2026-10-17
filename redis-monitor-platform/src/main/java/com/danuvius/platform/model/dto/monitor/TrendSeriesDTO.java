package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class TrendSeriesDTO implements Serializable {
    private String instanceId;

    /* 网络带宽返回出入两条子序列时，用 metricKey 区分子序列（netInputBandwidth / netOutputBandwidth），单指标时等于请求的 metricKey */
    private String metricKey;
    private List<Map<String, Object>> points;
}