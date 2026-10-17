package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TrendResponse implements Serializable {
    private String metricName;

    private String unit;
    private List<String> timestampList;
    private List<TrendSeriesDTO> seriesList;
}