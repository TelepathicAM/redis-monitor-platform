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

    /**
     * 执行器侧自检 Redis 是否就绪
     * true：本地已安装且能正常连接 Redis，本次携带 redisInfo 用于平台绑定实例；
     * false / null：Redis 未就绪，平台忽略 redisInfo，仅注册执行器。
     */
    private Boolean redisReady;
}
