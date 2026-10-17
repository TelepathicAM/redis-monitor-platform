package com.danuvius.platform.model.dto.monitor;

import com.danuvius.platform.model.dto.metric.MetricCurrentValueDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MetricCurrentValueResponse implements Serializable {
    private List<MetricCurrentValueDTO> metrics;
}