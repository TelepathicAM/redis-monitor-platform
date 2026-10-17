package com.danuvius.platform.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("executor_info")
public class ExecutorInfo {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String executorId;
    private String instanceId;
    private String hostIp;
    private String executorPort;
    /**
     * 执行器状态  0=OFFLINE / 1=ONLINE
     */
    private String executorStatus;
    private LocalDateTime lastHeartbeatTime;
    /**
     * 最近1次redis/Sentinel状态上报时间
     */
    private LocalDateTime lastReportTime;
    /**
     * 纳管状态：0=INACTIVE / 1=ACTIVE
     */
    private String managementStatus;
    /**
     * 认证 token
     */
    private String executorToken;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
