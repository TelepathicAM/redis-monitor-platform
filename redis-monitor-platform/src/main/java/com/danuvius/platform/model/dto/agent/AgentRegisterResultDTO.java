package com.danuvius.platform.model.dto.agent;

import lombok.Data;

/**
 * Agent 注册返回结构
 */
@Data
public class AgentRegisterResultDTO {

    /** 执行器后续心跳使用的 token */
    private String executorToken;

    /** 平台侧绑定的实例id；未绑定时为 null，执行器据此判断是否需要二次注册 */
    private String instanceId;

    /** 是否已完成实例绑定（= instanceId != null） */
    private Boolean bound;

    public AgentRegisterResultDTO(String executorToken) {
        this.executorToken = executorToken;
        this.instanceId = "";
        this.bound = false;
    }

    public AgentRegisterResultDTO(String executorToken, String instanceId) {
        this.executorToken = executorToken;
        this.instanceId = instanceId==null?"":instanceId;
        this.bound = instanceId != null && !instanceId.trim().isEmpty();
    }
}