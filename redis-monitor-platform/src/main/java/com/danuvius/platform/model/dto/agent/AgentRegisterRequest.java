package com.danuvius.platform.model.dto.agent;

import lombok.Data;

import java.util.Map;

/**
 * Agent 注册请求参数
 */
@Data
public class AgentRegisterRequest {

    /*注册码*/
    private String registerCode;

    /*执行器所在服务器IP*/
    private String hostIp;

    /*执行器 HTTPS 服务端口*/
    private String executorPort;

    /*随注册上报的Redis信息 */
    private Map<String, String> redisInfo;
}
