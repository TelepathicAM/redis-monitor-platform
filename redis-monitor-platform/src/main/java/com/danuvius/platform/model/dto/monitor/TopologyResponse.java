package com.danuvius.platform.model.dto.monitor;

import com.danuvius.platform.model.entity.RedisSwitchEvent;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TopologyResponse implements Serializable {
    private List<TopologyNodeDTO> nodes;
    private String status;
    private RedisSwitchEvent lastSwitch;
}