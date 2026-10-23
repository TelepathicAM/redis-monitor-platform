package com.danuvius.platform.util.validate;

import com.danuvius.platform.common.constant.CommonConstant;
import com.danuvius.platform.common.exception.validator.StringValidator;
import com.danuvius.platform.model.dto.agent.AgentHeartbeatRequest;
import org.apache.commons.lang3.StringUtils;


public class AgentHeartbeatRequestValidator {
    public static void validate(String auth,AgentHeartbeatRequest req) {
        StringValidator.notBlank(auth, "Authorization");

        StringValidator.notBlank(req.getHostIp(), "hostIp");
        StringValidator.maxLength(req.getHostIp(), 64, "hostIp");
        StringValidator.notBlank(req.getExecutorPort(), "executorPort");
        StringValidator.numeric(req.getExecutorPort(), "executorPort");


        StringValidator.notBlank(auth.replace("Bearer ", ""), "token");
    }
}
