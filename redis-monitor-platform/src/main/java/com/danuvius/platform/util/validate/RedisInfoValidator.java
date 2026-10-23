package com.danuvius.platform.util.validate;

import com.danuvius.platform.common.exception.ParameterCode;
import com.danuvius.platform.common.exception.ParameterException;
import com.danuvius.platform.model.dto.agent.AgentRegisterRequest;

import java.util.Map;

public class RedisInfoValidator {
    public static void validate(Map<String, String> redisInfo) {
        if(redisInfo== null || redisInfo.isEmpty()){
            throw new ParameterException(ParameterCode.PARAM_BLANK,
                    "字段[redis_info]不能为空");
        }
    }
}
