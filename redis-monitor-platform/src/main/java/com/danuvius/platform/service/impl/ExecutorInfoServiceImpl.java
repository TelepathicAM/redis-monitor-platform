package com.danuvius.platform.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.danuvius.platform.common.constant.CommonConstant;
import com.danuvius.platform.common.exception.BusinessCode;
import com.danuvius.platform.common.exception.BusinessException;
import com.danuvius.platform.mapper.ExecutorInfoMapper;
import com.danuvius.platform.mapper.RedisInstanceMapper;
import com.danuvius.platform.model.dto.agent.AgentRegisterResultDTO;
import com.danuvius.platform.model.entity.ExecutorInfo;
import com.danuvius.platform.model.entity.RedisInstance;
import com.danuvius.platform.model.enums.instance.RunStatusEnum;
import com.danuvius.platform.service.ExecutorInfoService;
import com.danuvius.platform.service.RedisInstanceService;
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
    private final RedisInstanceService redisInstanceService;

    @Value("${platform.register-code}")
    private List<String> validRegisterCodes;

    public ExecutorInfoServiceImpl(ExecutorInfoMapper executorInfoMapper, RedisInstanceMapper redisInstanceMapper,
                                   RedisInstanceService redisInstanceService) {
        this.executorInfoMapper = executorInfoMapper;
        this.redisInstanceMapper = redisInstanceMapper;
        this.redisInstanceService = redisInstanceService;
    }


    /**
     * 注册分两阶段，根据redisReady判断
     * redisReady=false / null：只注册执行器，不检查 redisInfo、不建实例
     * redisReady=true 且 redisInfo 非空：
     *      一阶段注册：未绑定实例-》注册执行器+建实例绑定
     *      二阶段注册：未绑定实例-》建实例绑定
     *      重复调用注册：已绑定实例-》直接返回已就绪
     * redisReady=true 但 redisInfo 为空：不绑定，返回未就绪
     */
    @Override
    public AgentRegisterResultDTO register(String registerCode,
                                           String hostIp, String executorPort,
                                           Map<String, String> redisInfo,
                                           Boolean redisReady) {
        validateRegisterCode(registerCode);

        // 就绪判定：redisReady=true+redisInfo非空
        Boolean readyToBind = Boolean.TRUE.equals(redisReady) && hasRedisInfo(redisInfo);

        //查是否已有执行器
        ExecutorInfo existing = executorInfoMapper.selectOne(Wrappers.<ExecutorInfo>lambdaQuery()
                .eq(ExecutorInfo::getHostIp, hostIp)
                .eq(ExecutorInfo::getExecutorPort, executorPort)
        );

        //已注册执行器
        if (existing != null) {
            // 已注册：仅在"未绑定 + 具备绑定条件"时才绑定
            if (!isBound(existing) && readyToBind) {
                bindInstance(existing, redisInfo);
            }
            log.info("Agent 重复注册: 执行器id={}, hostIp={}, redisReady={}, redis是否bind={}",
                    existing.getId(), hostIp, redisReady, isBound(existing));
            return buildResult(existing);
        }

        // 首次注册：只建执行器；redisReady=false 时不看 redisInfo
        ExecutorInfo agent = createExecutor(hostIp, executorPort);
        if (readyToBind) {
            //建redis实例表记录
            bindInstance(agent, redisInfo);
        }
        log.info("Agent 首次注册: 执行器id={}, hostIp={}, redisReady={}, bind={}",
                agent.getId(), hostIp, redisReady, isBound(agent));
        return buildResult(agent);
    }

    @Override
    public Boolean heartbeat(String executorToken, String hostIp, String executorPort) {
        ExecutorInfo agent = executorInfoMapper.selectOne(Wrappers.<ExecutorInfo>lambdaQuery()
                .eq(ExecutorInfo::getExecutorToken, executorToken)
        );
        if (agent == null ) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR.getCode(),"无效的注册码");
        }
        if (!hostIp.equals(agent.getHostIp()) || !executorPort.equals(agent.getExecutorPort())) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR.getCode(),"Agent 身份不匹配");
        }

        LocalDateTime now = LocalDateTime.now();
        //更新执行器状态字段
        int rows=executorInfoMapper.update(null,new LambdaUpdateWrapper<ExecutorInfo>()
                .set(ExecutorInfo::getLastHeartbeatTime,now)
                .set(ExecutorInfo::getExecutorStatus,CommonConstant.ONLINE)
                .set(ExecutorInfo::getUpdatedTime,now)
                .eq(ExecutorInfo::getExecutorToken,executorToken)
        );
        /* 执行器30s心跳时同步更新其绑定实例的最后心跳时间与运行状态 */
        if(rows>0) {
            log.info("执行器心跳更新:{}",CommonConstant.SUCCESS);
            // 已绑定时同步刷新实例心跳与运行状态；未绑定（S1 阶段）跳过
            if (isBound(agent)) {
                redisInstanceService.updateHeartbeatAndStatus(agent.getInstanceId(), now,
                        CommonConstant.RUN_NORMAL);
            }
            return true;
        }
        else{
            log.info("执行器心跳更新:{}",CommonConstant.FAILURE);
            return false;

        }
    }


    /*注册码校验*/
    private void validateRegisterCode(String registerCode) {
        if(validRegisterCodes ==null || validRegisterCodes.isEmpty()){
            throw new BusinessException(BusinessCode.NOT_FOUND_ERROR.getCode(), "平台比对注册码缺失");
        }

        if (!validRegisterCodes.contains(registerCode)) {
            throw new BusinessException(BusinessCode.PARAMS_ERROR.getCode(), "无效的注册码");
        }
    }

    /** 首次注册：instanceId 为空串，Redis 未就绪 */
    private ExecutorInfo createExecutor(String hostIp, String executorPort) {
        ExecutorInfo agent = new ExecutorInfo();
        agent.setInstanceId("");
        agent.setHostIp(hostIp);
        agent.setExecutorPort(executorPort);
        agent.setExecutorToken(IdUtil.simpleUUID());
        agent.setExecutorStatus(CommonConstant.ONLINE);
        // 平台手动纳管
        agent.setManagementStatus(CommonConstant.MANAGE_INACTIVE);
        LocalDateTime now = LocalDateTime.now();
        agent.setLastHeartbeatTime(now);
        agent.setCreatedTime(now);
        agent.setUpdatedTime(now);
        executorInfoMapper.insert(agent);
        return agent;
    }

    /**
     * 绑定实例，建redis实例表记录+更新执行器表实例id字段，若更新失败说明并发绑定，回滚刚建的实例并读取最新绑定结果
     */
    private void bindInstance(ExecutorInfo agent, Map<String, String> redisInfo) {
        String instanceId = IdUtil.getSnowflakeNextIdStr();
        RedisInstance instance = buildInstance(instanceId, agent.getHostIp(), redisInfo);
        redisInstanceMapper.insert(instance);

        int rows = executorInfoMapper.update(null, Wrappers.<ExecutorInfo>lambdaUpdate()
                .set(ExecutorInfo::getInstanceId, instanceId)
                .set(ExecutorInfo::getUpdatedTime, LocalDateTime.now())
                .eq(ExecutorInfo::getId, agent.getId())
                .eq(ExecutorInfo::getInstanceId, "")
        );

        if (rows == 0) {
            // 已被其他请求绑定：回滚新建实例，重新读取最新绑定
            redisInstanceMapper.deleteById(instance.getId());
            ExecutorInfo latest = executorInfoMapper.selectById(agent.getId());
            if (latest != null) {
                agent.setInstanceId(latest.getInstanceId());
            }
            log.warn("执行器 {} 并发绑定冲突，已回滚新建实例 {}", agent.getId(), instanceId);
            return;
        }
        agent.setInstanceId(instanceId);
        log.info("执行器 {} 绑定实例成功: {}", agent.getId(), instanceId);
    }

    /*建redis实体*/
    private RedisInstance buildInstance(String instanceId, String fallbackHostIp,
                                        Map<String, String> redisInfo) {
        RedisInstance instance = new RedisInstance();
        instance.setInstanceId(instanceId);

        String ip = redisInfo.getOrDefault("host", fallbackHostIp);
        String port = redisInfo.getOrDefault("tcp_port", CommonConstant.DEFAULT_REDIS_PORT);
        instance.setInstanceName(ip + ":" + port);
        instance.setHostIp(ip);
        instance.setRedisPort(port);

        String role = redisInfo.getOrDefault("role", CommonConstant.DEFAULT_ROLE_MASTER).toUpperCase();
        instance.setRole(role);
        if (CommonConstant.DEFAULT_ROLE_REPLICA.equals(role)) {
            instance.setMasterHostIp(redisInfo.get("master_host"));
            instance.setMasterRedisPort(redisInfo.get("master_port"));
        }
        instance.setRedisVersion(redisInfo.getOrDefault("version", ""));
        instance.setRunStatus(CommonConstant.RUN_NORMAL);
        // 平台手动纳管
        instance.setManagementStatus(CommonConstant.MANAGE_INACTIVE);
        LocalDateTime now = LocalDateTime.now();
        instance.setLastHeartbeatTime(now);
        instance.setCreatedTime(now);
        instance.setUpdatedTime(now);
        return instance;
    }

    /*返回结构构造*/
    private AgentRegisterResultDTO buildResult(ExecutorInfo agent) {
        return new AgentRegisterResultDTO(agent.getExecutorToken(),
                isBound(agent) ? agent.getInstanceId() : null);
    }

    /*执行器记录存在且实例id非空串，则已绑定*/
    private Boolean isBound(ExecutorInfo agent) {
        return agent != null && !agent.getInstanceId().trim().isEmpty();
    }

    /*判断是否存在redisInfo */
    private Boolean hasRedisInfo(Map<String, String> redisInfo) {
        return redisInfo != null && !redisInfo.isEmpty();
    }


}