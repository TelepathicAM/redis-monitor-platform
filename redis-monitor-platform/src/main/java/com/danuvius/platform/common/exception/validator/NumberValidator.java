package com.danuvius.platform.common.exception.validator;

import com.danuvius.platform.common.exception.ParameterCode;
import com.danuvius.platform.common.exception.ParameterException;
import org.apache.commons.lang3.StringUtils;

/**
 * 数字参数校验。
 * 消息格式："字段[xxx]xxx"。
 */
public final class NumberValidator {

    private NumberValidator() {
    }

    /** 数字非空 */
    public static void notNull(Number value, String fieldName) {
        if (value == null) {
            throw new ParameterException(ParameterCode.PARAM_NULL,
                    "字段[" + fieldName + "]不能为空");
        }
    }

    /** 必须为正数（>0） */
    public static void positive(Number value, String fieldName) {
        if (value == null) {
            return;
        }
        if (value.doubleValue() <= 0) {
            throw new ParameterException(ParameterCode.PARAM_RANGE_ERROR,
                    "字段[" + fieldName + "]必须大于0");
        }
    }

    /** 必须为非负数（>=0） */
    public static void nonNegative(Number value, String fieldName) {
        if (value == null) {
            return;
        }
        if (value.doubleValue() < 0) {
            throw new ParameterException(ParameterCode.PARAM_RANGE_ERROR,
                    "字段[" + fieldName + "]不能小于0");
        }
    }

    /** 必须为整数 */
    public static void integer(Number value, String fieldName) {
        if (value == null) {
            return;
        }
        double d = value.doubleValue();
        if (d != Math.floor(d) || Double.isInfinite(d)) {
            throw new ParameterException(ParameterCode.PARAM_FORMAT_ERROR,
                    "字段[" + fieldName + "]必须为整数");
        }
    }

    /** 最小精度（小数位） */
    public static void scaleBetween(Number value, int minScale, int maxScale, String fieldName) {
        if (value == null) {
            return;
        }
        String text = value.toString();
        int dot = text.indexOf('.');
        int scale = dot < 0 ? 0 : text.length() - dot - 1;
        if (scale < minScale || scale > maxScale) {
            throw new ParameterException(ParameterCode.PARAM_FORMAT_ERROR,
                    "字段[" + fieldName + "]小数位必须在" + minScale + "~" + maxScale + "之间");
        }
    }

    /** 解析为 Integer（空白返回 null） */
    public static Integer parseInteger(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new ParameterException(ParameterCode.PARAM_TYPE_ERROR,
                    "字段[" + fieldName + "]必须为整数");
        }
    }

    /** 解析为 Long（空白返回 null） */
    public static Long parseLong(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new ParameterException(ParameterCode.PARAM_TYPE_ERROR,
                    "字段[" + fieldName + "]必须为整数");
        }
    }


    /** 解析为 Double（空白返回 null；拒绝 NaN/Infinity） */
    public static Double parseDouble(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            Double d = Double.valueOf(value.trim());
            // 关键修复：NaN / Infinity 不是合法业务数值
            if (d.isNaN() || d.isInfinite()) {
                throw new ParameterException(ParameterCode.PARAM_FORMAT_ERROR,
                        "字段[" + fieldName + "]必须为有限数字");
            }
            return d;
        } catch (NumberFormatException e) {
            throw new ParameterException(ParameterCode.PARAM_TYPE_ERROR,
                    "字段[" + fieldName + "]必须为数字");
        }
    }

    /** 范围 [min, max]（闭区间），对 NaN/Infinity 也做防御 */
    public static void valueBetween(Number value, Number min, Number max, String fieldName) {
        if (value == null) {
            return;
        }
        double v = value.doubleValue();
        if (Double.isNaN(v) || Double.isInfinite(v)) {
            throw new ParameterException(ParameterCode.PARAM_FORMAT_ERROR,
                    "字段[" + fieldName + "]必须为有限数字");
        }
        if (v < min.doubleValue() || v > max.doubleValue()) {
            throw new ParameterException(ParameterCode.PARAM_RANGE_ERROR,
                    "字段[" + fieldName + "]必须在" + min + "~" + max + "之间");
        }
    }

// ===== 以下为新增组合方法 =====

    /** 解析为 Double 并校验非负；空白返回 null */
    public static Double parseDoubleNonNegative(String value, String fieldName) {
        Double d = parseDouble(value, fieldName);
        nonNegative(d, fieldName);
        return d;
    }

    /** 解析为 Double 并校验闭区间 [min, max]；空白返回 null */
    public static Double parseDoubleBetween(String value, double min, double max, String fieldName) {
        Double d = parseDouble(value, fieldName);
        valueBetween(d, min, max, fieldName);
        return d;
    }

    /** 解析为 Integer 并校验闭区间 [min, max]；空白返回 null */
    public static Integer parseIntegerBetween(String value, int min, int max, String fieldName) {
        Integer i = parseInteger(value, fieldName);
        valueBetween(i, min, max, fieldName);
        return i;
    }


    public static void requiredDoubleBetween(String raw, double min, double max, String field) {
        StringValidator.notBlank(raw,field);
        Double v = NumberValidator.parseDoubleBetween(raw, min, max, field);
        NumberValidator.notNull(v, field);
    }

    public static void requiredIntegerBetween(String raw, int min, int max, String field) {
        StringValidator.notBlank(raw,field);

        Integer v = NumberValidator.parseIntegerBetween(raw, min, max, field);
        NumberValidator.notNull(v, field);
    }
}