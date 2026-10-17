package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;

@Data
public class NodeStatusResponse implements Serializable {
    private String instanceId;
    private String instanceName;
    private String ip;
    private String port;
    private String role;
    private String masterName;
    private String copyDelay;
    private String copyStatus;
    private String runStatus;
}