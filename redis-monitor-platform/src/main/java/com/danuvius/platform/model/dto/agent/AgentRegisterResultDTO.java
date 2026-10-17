package com.danuvius.platform.model.dto.agent;

import lombok.Data;

/**
 * Agent 注册返回结构
 */
@Data
public class AgentRegisterResultDTO {

    /** 执行器后续心跳使用的 token */
    private String executorToken;

    public AgentRegisterResultDTO(String executorToken) {
        this.executorToken = executorToken;
    }

}
