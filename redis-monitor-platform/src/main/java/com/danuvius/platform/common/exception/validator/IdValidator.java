package com.danuvius.platform.common.exception.validator;

import com.danuvius.platform.common.exception.ParameterCode;
import com.danuvius.platform.common.exception.ParameterException;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * ID 参数校验。
 * 雪花ID字符串、数字ID、普通字符串ID。
 */
public final class IdValidator {

    /** 雪花ID部分：18~19 位十进制，与 Hutool getSnowflakeNextIdStr() 对齐 */
    private static final String SNOWFLAKE_BODY = "\\d{18,19}";

    private static final Pattern SIMPLE_UUID = Pattern.compile("^[0-9a-f]{32}$");

    /** 前缀 -> 编译好的 Pattern 缓存 */
    private static final ConcurrentHashMap<String, Pattern> CACHE = new ConcurrentHashMap<>();

    private IdValidator() {
    }

    public static void checkUUID(String uuid,String fieldName) {
        notBlank(uuid, fieldName);
        if (!SIMPLE_UUID.matcher(uuid).matches()) {
            throw new ParameterException(ParameterCode.PARAM_ID_ERROR,
                    "字段[" + fieldName + "]ID格式不正确");
        }
    }


    /** 按前缀拿 Pattern */
    public static Pattern prefixOf(String prefix) {
        return CACHE.computeIfAbsent(prefix,
                p -> Pattern.compile("^" + Pattern.quote(p) + SNOWFLAKE_BODY + "$"));
    }

    /** 格式校验：前缀 + 位数 + 总长度 */
    public static boolean isValid(String id, String prefix, int maxLen) {
        return id != null
                && id.length() <= maxLen
                && prefixOf(prefix).matcher(id).matches();
    }
    /** 校验业务id格式是否符合xx_雪花id的形式 */
    public static void check(String id, String prefix, int maxLen, String fieldName) {
        if (!isValid(id, prefix, maxLen)) {
            throw new ParameterException(ParameterCode.PARAM_FORMAT_ERROR,
                    "字段[" + fieldName + "]格式非法，应为 " + prefix + " + 18~19位数字");
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

    /** 业务前缀ID，如 RL_xxx、EP_xxx：非空且不含空白 */
    public static void prefixed(String id, String fieldName) {
        notBlank(id, fieldName);

        if (id.trim().length() != id.length()) {
            throw new ParameterException(ParameterCode.PARAM_ID_ERROR,
                    "字段[" + fieldName + "]ID格式不正确");
        }
    }
}