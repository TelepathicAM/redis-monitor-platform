package com.danuvius.platform.util.validate;

import com.danuvius.platform.common.exception.ParameterCode;
import com.danuvius.platform.common.exception.ParameterException;
import com.danuvius.platform.common.exception.validator.StringValidator;
import com.danuvius.platform.model.dto.agent.AgentRegisterRequest;

public class AgentRegisterRequestValidator {
    public static void validate(AgentRegisterRequest req) {
        StringValidator.notBlank(req.getRegisterCode(), "registerCode");
        StringValidator.notBlank(req.getHostIp(), "hostIp");
        StringValidator.maxLength(req.getHostIp(), 64, "hostIp");
        StringValidator.notBlank(req.getExecutorPort(), "executorPort");
        StringValidator.numeric(req.getExecutorPort(), "executorPort");
        if(req.getRedisReady()==null){
            throw new ParameterException(ParameterCode.PARAM_BLANK,
                    "字段[redisReady]不能为空");
        }
        /*redisInfo放到Service层校验，配合注册逻辑*/
    }
}
