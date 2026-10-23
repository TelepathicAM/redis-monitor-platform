package com.danuvius.platform.util.validate;

import com.danuvius.platform.common.constant.CommonConstant;
import com.danuvius.platform.common.exception.validator.IdValidator;
import com.danuvius.platform.common.exception.validator.NumberValidator;
import com.danuvius.platform.common.exception.validator.StringValidator;
import com.danuvius.platform.model.dto.alarm.rule.RuleToggleRequest;

public class RuleToggleRequestValidator {
    public static void validate(RuleToggleRequest req){
        StringValidator.notBlank(req.getRuleId(), "ruleId");
        IdValidator.check(req.getRuleId(),"RL_",32,"ruleId");


        StringValidator.isEnumValue(req.getEnableStatus(), CommonConstant.ENABLE_STATUS, "enableStatus");
    }
}
