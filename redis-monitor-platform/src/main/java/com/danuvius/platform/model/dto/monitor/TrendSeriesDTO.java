package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class TrendSeriesDTO implements Serializable {
    /** 实例id */
    private String instanceId;

    /* 网络带宽返回出入两条子序列时，用 metricKey 区分子序列（netInputBandwidth / netOutputBandwidth），单指标时等于请求的 metricKey */
    /** 子序列指标 key */
    private String metricKey;
    /** 时间点数据列表，每项含 bucket 与 value（或 inValue / outValue） */
    private List<Map<String, Object>> points;
}