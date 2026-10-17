package com.danuvius.platform.model.dto.agent;

import lombok.Data;

/**
 * Agent 心跳请求参数
 */
@Data
public class AgentHeartbeatRequest {

    /*执行器所在服务器IP*/
    private String hostIp;
    /*执行器服务端口*/
    private String executorPort;
    /*执行器状态：0=OFFLINE/1=ONLINE*/
    private String executorStatus;
}