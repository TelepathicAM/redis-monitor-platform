package com.danuvius.platform.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.danuvius.platform.mapper.ExecutorInfoMapper;
import com.danuvius.platform.mapper.RedisInstanceMapper;
import com.danuvius.platform.model.dto.agent.AgentRegisterResultDTO;
import com.danuvius.platform.model.entity.ExecutorInfo;
import com.danuvius.platform.model.entity.RedisInstance;
import com.danuvius.platform.model.enums.StatusEnum;
import com.danuvius.platform.service.ExecutorInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ExecutorInfoServiceImpl extends ServiceImpl<ExecutorInfoMapper, ExecutorInfo>
        implements ExecutorInfoService {

    private final ExecutorInfoMapper executorInfoMapper;
    private final RedisInstanceMapper redisInstanceMapper;

    @Value("${platform.register-code}")
    private List<String> validRegisterCodes;

    public ExecutorInfoServiceImpl(ExecutorInfoMapper executorInfoMapper, RedisInstanceMapper redisInstanceMapper) {
        this.executorInfoMapper = executorInfoMapper;
        this.redisInstanceMapper = redisInstanceMapper;
    }

    @Override
    public AgentRegisterResultDTO register(String registerCode,
                                           String hostIp, String executorPort,
                                           Map<String, String> redisInfo) {
        if (registerCode == null || !validRegisterCodes.contains(registerCode)) {
            throw new RuntimeException("无效的注册码");
        }

        ExecutorInfo existing = executorInfoMapper.selectByHostIpAndPort(hostIp, executorPort);
        if (existing != null) {
            return new AgentRegisterResultDTO(existing.getExecutorToken());
        }

        // executor_id 使用雪花生成
        String executorId = IdUtil.getSnowflakeNextIdStr();
        String instanceId = null;

        // 根据 redisInfo 先创建 RedisInstance 记录
        if (redisInfo != null && !redisInfo.isEmpty()) {
            instanceId = IdUtil.getSnowflakeNextIdStr();
            RedisInstance instance = new RedisInstance();

            instance.setInstanceId(instanceId);

            String ip = redisInfo.getOrDefault("host", hostIp);
            String port = redisInfo.getOrDefault("tcp_port", "6379");

            instance.setInstanceName(ip+":"+port);
            instance.setHostIp(ip);
            instance.setRedisPort(port);

            instance.setRole(redisInfo.getOrDefault("role", "MASTER"));
            instance.setRedisVersion(redisInfo.getOrDefault("version", ""));
            instance.setRunStatus(StatusEnum.RUN_NORMAL.getCode());
            instance.setManagementStatus("1");
            instance.setLastHeartbeatTime(LocalDateTime.now());
            instance.setCreatedTime(LocalDateTime.now());
            instance.setUpdatedTime(LocalDateTime.now());
            redisInstanceMapper.insert(instance);
        }
        //如果redisInfo为空，instanceId就空，后续指标上报应报“执行器未绑定实例”
        ExecutorInfo agent = new ExecutorInfo();
        agent.setExecutorId(executorId);
        agent.setInstanceId(instanceId);
        agent.setHostIp(hostIp);
        agent.setExecutorPort(executorPort);
        agent.setExecutorToken(IdUtil.simpleUUID());
        agent.setExecutorStatus("1");
        agent.setManagementStatus("1");
        agent.setLastHeartbeatTime(LocalDateTime.now());
        agent.setCreatedTime(LocalDateTime.now());
        agent.setUpdatedTime(LocalDateTime.now());
        executorInfoMapper.insert(agent);

        log.info("Agent 注册成功: id={}, hostIp={}", agent.getId(), hostIp);
        return new AgentRegisterResultDTO(agent.getExecutorToken());
    }

    @Override
    public Object heartbeat(String executorToken, String hostIp, String executorPort, String executorStatus) {
        ExecutorInfo agent = executorInfoMapper.selectByToken(executorToken);
        if (agent == null ) {
            throw new RuntimeException("无效的 Agent token");
        }
        if (!hostIp.equals(agent.getHostIp()) || !executorPort.equals(agent.getExecutorPort())) {
            throw new RuntimeException("Agent 身份不匹配");
        }
        LocalDateTime now = LocalDateTime.now();
        agent.setLastHeartbeatTime(now);
        agent.setExecutorStatus(executorStatus != null ? executorStatus : "1");
        agent.setUpdatedTime(now);
        executorInfoMapper.updateById(agent);

        /* 执行器30s心跳时同步更新其绑定实例的最后心跳时间与运行状态 */
        if (agent.getInstanceId() != null && !agent.getInstanceId().trim().isEmpty()) {
            redisInstanceMapper.updateHeartbeatAndStatus(agent.getInstanceId(), now,
                    StatusEnum.RUN_NORMAL.getCode());
        }
        return null;
    }

    @Override
    public ExecutorInfo findByToken(String executorToken) {
        return executorInfoMapper.selectByToken(executorToken);
    }
}