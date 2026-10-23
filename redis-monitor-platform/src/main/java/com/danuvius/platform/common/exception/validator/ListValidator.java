package com.danuvius.platform.common.exception.validator;

import com.danuvius.platform.common.exception.ParameterCode;
import com.danuvius.platform.common.exception.ParameterException;

import java.util.List;

/**
 * 集合 / Map 参数校验。
 */
public final class ListValidator {

    private ListValidator() {
    }

    /** 集合非空（非 null 且 size > 0） */
    public static void notEmpty(List<String> value, String fieldName) {
        if (value == null || value.isEmpty()) {
            throw new ParameterException(ParameterCode.PARAM_COLLECTION_EMPTY,
                    "字段[" + fieldName + "]不能为空");
        }
    }




    /** 集合最大大小 */
    public static void maxSize(List<String> value, int max, String fieldName) {
        if (value == null) {
            return;
        }
        if (value.size() > max) {
            throw new ParameterException(ParameterCode.PARAM_COLLECTION_SIZE_ERROR,
                    "字段[" + fieldName + "]元素数量不能超过" + max);
        }
    }

    /** 集合内不允许 null 元素 */
    public static void noNullElement(List<String> value, String fieldName) {
        if (value == null) {
            return;
        }
        for (Object item : value) {
            if (item == null) {
                throw new ParameterException(ParameterCode.PARAM_INVALID,
                        "字段[" + fieldName + "]不允许包含空元素");
            }
        }
    }

    /** 集合内字符串不允许空白 */
    public static void noBlankStringElement(List<String> value, String fieldName) {
        if (value == null) {
            return;
        }
        for (String item : value) {
            if (item == null || item.trim().isEmpty()) {
                throw new ParameterException(ParameterCode.PARAM_INVALID,
                        "字段[" + fieldName + "]不允许包含空白元素");
            }
        }
    }
}