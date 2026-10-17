package com.danuvius.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.danuvius.platform.model.dto.agent.AgentRegisterResultDTO;
import com.danuvius.platform.model.entity.ExecutorInfo;

import java.util.Map;

public interface ExecutorInfoService extends IService<ExecutorInfo> {

    AgentRegisterResultDTO register(String registerCode, String hostIp, String executorPort,
                                    Map<String, String> redisInfo);

    Object heartbeat(String executorToken, String hostIp, String executorPort, String executorStatus);

    ExecutorInfo findByToken(String executorToken);
}