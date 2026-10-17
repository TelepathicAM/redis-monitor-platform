package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class RedisInstanceItemDTO implements Serializable {
    private String instanceId;
    private String instanceName;
    private String hostIp;
    private String redisPort;
    private String role;
    private String redisVersion;
    /** 正常 / 警告 / 严重 / 离线 */
    private String runStatus;
    private LocalDateTime lastHeartbeatTime;
    private String masterHostIp;
    private String masterRedisPort;
}