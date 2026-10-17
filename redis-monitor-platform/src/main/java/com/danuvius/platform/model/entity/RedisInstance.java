package com.danuvius.platform.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("redis_instance")
public class RedisInstance {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String instanceId;
    private String instanceName;
    private String hostIp;
    private String redisPort;
    private String role;
    private String redisVersion;
    private String runStatus;
    private String masterHostIp;
    private String masterRedisPort;
    private LocalDateTime lastHeartbeatTime;

    private String managementStatus;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

}
