package com.danuvius.platform.common.exception.validator;

import com.danuvius.platform.common.exception.ParameterCode;
import com.danuvius.platform.common.exception.ParameterException;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 字符串参数校验。
 * 消息格式："字段[xxx]xxx"。
 */
public final class StringValidator {

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_CN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern URL = Pattern.compile("^https?://[\\w\\-.:/?=&%#]+$");

    private StringValidator() {
    }

    /** 非空白（null 或 "" 或全空白均失败） */
    public static void notBlank(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            throw new ParameterException(ParameterCode.PARAM_BLANK,
                    "字段[" + fieldName + "]不能为空");
        }
    }

    /** 非空（"" 允许，null 不允许） */
    public static void notNull(String value, String fieldName) {
        if (value == null) {
            throw new ParameterException(ParameterCode.PARAM_NULL,
                    "字段[" + fieldName + "]不能为空");
        }
    }

    /** 最大长度 */
    public static void maxLength(String value, int max, String fieldName) {
        if (value != null && value.length() > max) {
            throw new ParameterException(ParameterCode.PARAM_LENGTH_ERROR,
                    "字段[" + fieldName + "]长度不能超过" + max);
        }
    }

    /** 最小长度 */
    public static void minLength(String value, int min, String fieldName) {
        if (value != null && value.length() < min) {
            throw new ParameterException(ParameterCode.PARAM_LENGTH_ERROR,
                    "字段[" + fieldName + "]长度不能少于" + min);
        }
    }

    /** 长度区间 [min, max] */
    public static void lengthBetween(String value, int min, int max, String fieldName) {
        if (value == null) {
            return;
        }
        if (value.length() < min || value.length() > max) {
            throw new ParameterException(ParameterCode.PARAM_LENGTH_ERROR,
                    "字段[" + fieldName + "]长度必须在" + min + "~" + max + "之间");
        }
    }

    /** 正则匹配 */
    public static void matches(String value, String regex, String fieldName) {
        if (value == null) {
            return;
        }
        if (!Pattern.matches(regex, value)) {
            throw new ParameterException(ParameterCode.PARAM_REGEX_ERROR,
                    "字段[" + fieldName + "]格式不正确");
        }
    }

    /** 邮箱 */
    public static void email(String value, String fieldName) {
        if (value == null) {
            return;
        }
        if (!EMAIL.matcher(value).matches()) {
            throw new ParameterException(ParameterCode.PARAM_REGEX_ERROR,
                    "字段[" + fieldName + "]邮箱格式不正确");
        }
    }

    /** 中国大陆手机号 */
    public static void phone(String value, String fieldName) {
        if (value == null) {
            return;
        }
        if (!PHONE_CN.matcher(value).matches()) {
            throw new ParameterException(ParameterCode.PARAM_REGEX_ERROR,
                    "字段[" + fieldName + "]手机号格式不正确");
        }
    }

    /** URL */
    public static void url(String value, String fieldName) {
        if (value == null) {
            return;
        }
        if (!URL.matcher(value).matches()) {
            throw new ParameterException(ParameterCode.PARAM_REGEX_ERROR,
                    "字段[" + fieldName + "]URL格式不正确");
        }
    }

    /** 必须是纯数字字符串 */
    public static void numeric(String value, String fieldName) {
        if (value == null) {
            return;
        }
        if (!StringUtils.isNumeric(value)) {
            throw new ParameterException(ParameterCode.PARAM_FORMAT_ERROR,
                    "字段[" + fieldName + "]必须为数字");
        }
    }
}