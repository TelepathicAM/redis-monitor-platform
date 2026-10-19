package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SentinelItemResponse implements Serializable {
    /** 哨兵节点id */
    private String sentinelId;
    /** 哨兵节点名 */
    private String sentinelName;
    /** 哨兵节点IP */
    private String sentinelIp;
    /** 哨兵节点端口 */
    private String sentinelPort;
    /** 在线状态：在线 / 离线 */
    private String sentinelStatus;
    /** 最后心跳时间，无心跳返回 null */
    private LocalDateTime lastHeartbeatTime;
}