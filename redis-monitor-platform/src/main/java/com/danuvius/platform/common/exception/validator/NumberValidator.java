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

    /** 范围 [min, max]（闭区间） */
    public static void between(Number value, Number min, Number max, String fieldName) {
        if (value == null) {
            return;
        }
        double v = value.doubleValue();
        if (v < min.doubleValue() || v > max.doubleValue()) {
            throw new ParameterException(ParameterCode.PARAM_RANGE_ERROR,
                    "字段[" + fieldName + "]必须在" + min + "~" + max + "之间");
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

    /** 解析为 Double（空白返回 null） */
    public static Double parseDouble(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new ParameterException(ParameterCode.PARAM_TYPE_ERROR,
                    "字段[" + fieldName + "]必须为数字");
        }
    }
}