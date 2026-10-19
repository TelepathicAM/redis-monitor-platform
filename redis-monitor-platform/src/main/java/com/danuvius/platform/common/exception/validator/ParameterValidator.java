package com.danuvius.platform.common.exception.validator;

import com.danuvius.platform.common.exception.ParameterCode;
import com.danuvius.platform.common.exception.ParameterException;

import java.util.Collection;

/**
 * 通用参数校验入口。
 * 所有校验失败统一抛 ParameterException，消息格式："字段[xxx]xxx"
 */
public final class ParameterValidator {

    private ParameterValidator() {
    }

    // ---------------- 非空 ----------------

    /** 对象非空 */
    public static void notNull(Object value, String fieldName) {
        if (value == null) {
            throw new ParameterException(ParameterCode.PARAM_NULL, "字段[" + fieldName + "]不能为空");
        }
    }

    /** 对象必须为 null（用于必须不传的场景） */
    public static void isNull(Object value, String fieldName) {
        if (value != null) {
            throw new ParameterException(ParameterCode.PARAM_INVALID, "字段[" + fieldName + "]不允许传值");
        }
    }

    // ---------------- 布尔断言 ----------------

    /** 断言为 true，否则抛异常 */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new ParameterException(ParameterCode.PARAM_INVALID, message);
        }
    }

    /** 断言为 false，否则抛异常 */
    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throw new ParameterException(ParameterCode.PARAM_INVALID, message);
        }
    }

    /** 断言为 true，自定义错误码 */
    public static void isTrue(boolean expression, ParameterCode code, String message) {
        if (!expression) {
            throw new ParameterException(code, message);
        }
    }

    // ---------------- 相等 / 枚举 ----------------

    /** 两对象必须相等 */
    public static void equals(Object expected, Object actual, String fieldName) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || !expected.equals(actual)) {
            throw new ParameterException(ParameterCode.PARAM_INVALID,
                    "字段[" + fieldName + "]取值不合法");
        }
    }

    /** 值必须在允许集合内 */
    public static void in(Object value, Collection<?> allowed, String fieldName) {
        if (value == null || allowed == null || !allowed.contains(value)) {
            throw new ParameterException(ParameterCode.PARAM_ENUM_ERROR,
                    "字段[" + fieldName + "]不在允许范围内");
        }
    }

    /** 字符串必须为指定枚举之一 */
    public static void in(String value, String[] allowed, String fieldName) {
        if (value == null || allowed == null) {
            throw new ParameterException(ParameterCode.PARAM_ENUM_ERROR,
                    "字段[" + fieldName + "]不在允许范围内");
        }
        for (String item : allowed) {
            if (item.equals(value)) {
                return;
            }
        }
        throw new ParameterException(ParameterCode.PARAM_ENUM_ERROR,
                "字段[" + fieldName + "]不在允许范围内");
    }
}