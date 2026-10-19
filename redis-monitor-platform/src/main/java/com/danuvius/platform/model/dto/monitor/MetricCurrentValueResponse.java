package com.danuvius.platform.model.dto.monitor;

import com.danuvius.platform.model.dto.metric.MetricCurrentValueDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MetricCurrentValueResponse implements Serializable {
    /** 核心实时指标列表（6 项） */
    private List<MetricCurrentValueDTO> metrics;
}