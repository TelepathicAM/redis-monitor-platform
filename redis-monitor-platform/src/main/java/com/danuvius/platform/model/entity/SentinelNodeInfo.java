package com.danuvius.platform.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sentinel_node_info")
public class SentinelNodeInfo {
    /**
     * 哨兵节点id，host:port
     */
    @TableId(type = IdType.INPUT)
    private String id;
    private String nodeName;
    private String nodeIp;
    private String nodePort;
    private String sentinelStatus;
    private LocalDateTime lastHeartbeatTime;

    private String managementStatus;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
