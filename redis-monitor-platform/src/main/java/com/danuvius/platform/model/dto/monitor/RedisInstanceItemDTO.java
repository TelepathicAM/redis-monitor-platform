package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 前端展示项
 */
@Data
public class RedisInstanceItemDTO implements Serializable {
    /*实例id*/
    private String instanceId;
    /*实例名*/
    private String instanceName;
    /*服务器ip*/
    private String hostIp;
    /*redis端口*/
    private String redisPort;
    /*节点角色*/
    private String role;
    /*当前redis版本号*/
    private String redisVersion;
    /** 节点运行状态：正常 / 警告 / 严重 / 离线 */
    private String runStatus;
    /*最近心跳时间*/
    private LocalDateTime lastHeartbeatTime;
    /*主节点ip*/
    private String masterHostIp;
    /*主节点redis端口*/
    private String masterRedisPort;
}