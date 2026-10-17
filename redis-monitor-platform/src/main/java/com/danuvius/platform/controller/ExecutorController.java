package com.danuvius.platform.controller;

import com.danuvius.platform.common.BaseResponse;
import com.danuvius.platform.common.ResultUtils;
import com.danuvius.platform.common.exception.BusinessCode;
import com.danuvius.platform.model.dto.agent.AgentHeartbeatRequest;
import com.danuvius.platform.model.dto.agent.AgentRegisterRequest;
import com.danuvius.platform.model.dto.agent.AgentRegisterResultDTO;
import com.danuvius.platform.service.ExecutorInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/agent")
@Validated
public class ExecutorController {

    private final ExecutorInfoService executorInfoService;

    public ExecutorController(ExecutorInfoService executorInfoService) {
        this.executorInfoService = executorInfoService;
    }

    @PostMapping("/register")
    public BaseResponse<AgentRegisterResultDTO> register(@Valid @RequestBody AgentRegisterRequest req) {
        try {
            AgentRegisterResultDTO result = executorInfoService.register(
                    req.getRegisterCode(),
                    req.getHostIp(), req.getExecutorPort(), req.getRedisInfo());
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(BusinessCode.FORBIDDEN_ERROR.OPERATION_ERROR.getCode(), e.getMessage());
        }
    }

    @PostMapping("/heartbeat")
    public BaseResponse<Object> heartbeat(@RequestHeader("Authorization") @NotBlank(message = "Authorization不能为空") String auth,
                                          @Valid @RequestBody AgentHeartbeatRequest req) {
        String token = auth.replace("Bearer ", "");
        try {
            Object tasks = executorInfoService.heartbeat(
                    token, req.getHostIp(), req.getExecutorPort(), req.getExecutorStatus());
            return ResultUtils.success(tasks);
        } catch (Exception e) {
            return ResultUtils.error(BusinessCode.OPERATION_ERROR.getCode(), e.getMessage());
        }
    }
}