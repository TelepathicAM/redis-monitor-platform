package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SentinelItemResponse implements Serializable {
    private String sentinelId;
    private String sentinelName;
    private String sentinelIp;
    private String sentinelPort;
    private String sentinelStatus;
    private LocalDateTime lastHeartbeatTime;
}