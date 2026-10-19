package com.danuvius.platform.common.exception.validator;

import com.danuvius.platform.common.exception.ParameterCode;
import com.danuvius.platform.common.exception.ParameterException;
import org.apache.commons.lang3.StringUtils;

/**
 * ID 参数校验。
 * 雪花ID字符串、数字ID、普通字符串ID。
 */
public final class IdValidator {

    private IdValidator() {
    }

    /** 数字ID非空且 > 0 */
    public static void notNull(Long id, String fieldName) {
        if (id == null || id <= 0) {
            throw new ParameterException(ParameterCode.PARAM_NULL,
                    "字段[" + fieldName + "]不能为空");
        }
    }

    /** 字符串ID非空 */
    public static void notBlank(String id, String fieldName) {
        if (StringUtils.isBlank(id)) {
            throw new ParameterException(ParameterCode.PARAM_BLANK,
                    "字段[" + fieldName + "]不能为空");
        }
    }

    /** 雪花ID字符串：非空且纯数字 */
    public static void snowflake(String id, String fieldName) {
        if (StringUtils.isBlank(id)) {
            throw new ParameterException(ParameterCode.PARAM_BLANK,
                    "字段[" + fieldName + "]不能为空");
        }
        if (!StringUtils.isNumeric(id)) {
            throw new ParameterException(ParameterCode.PARAM_ID_ERROR,
                    "字段[" + fieldName + "]ID格式不正确");
        }
    }

    /** 业务前缀ID，如 RL_xxx、export_xxx：非空且不含空白 */
    public static void prefixed(String id, String fieldName) {
        notBlank(id, fieldName);
        if (id.trim().length() != id.length()) {
            throw new ParameterException(ParameterCode.PARAM_ID_ERROR,
                    "字段[" + fieldName + "]ID格式不正确");
        }
    }
}