package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;

@Data
public class TopologyNodeDTO implements Serializable {
    private String instanceId;
    private String instanceName;
    private String ip;
    private String port;
    private String role;
    private String masterName;
    private String copyDelay;
    /*复制状态： 0=正常/1=延迟*/
    private String copyStatus;
    /*整体拓扑状态： 0=健康/1=警告/2=异常*/
    private String topologyStatus;
}