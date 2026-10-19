package com.danuvius.platform.model.dto.monitor;

import com.danuvius.platform.model.entity.RedisSwitchEvent;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TopologyResponse implements Serializable {
    /** 拓扑节点列表 */
    private List<TopologyNodeDTO> nodes;
    /** 整体拓扑状态：健康 / 警告 / 异常 */
    private String topologyStatus;
    /** 最近一次主从切换事件 */
    private RedisSwitchEvent lastSwitch;
}