package com.danuvius.platform.model.enums.rule;

import lombok.Getter;

@Getter
public enum EnableStatusEnum {
    /**
     * alarm_rule表
     */
    /*enableStatus*/
    DISABLE("0","禁用"),
    ENABLE("1","启用");

    private final String code;
    private final String desc;

    EnableStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**数字转文字*/
    public static String descOf(String code) {
        if (code == null) {
            return null;
        }
        for (EnableStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e.desc;
            }
        }
        // 已是文字的直接返回
        for (EnableStatusEnum e : values()) {
            if (e.desc.equals(code)) {
                return e.desc;
            }
        }
        return code;
    }

    /**文字转数字*/
    public static String codeOf(String desc) {
        if (desc == null) {
            return null;
        }
        for (EnableStatusEnum e : values()) {
            if (e.desc.equals(desc)) {
                return e.code;
            }
        }
        for (EnableStatusEnum e : values()) {
            if (e.code.equals(desc)) {
                return e.code;
            }
        }
        return desc;
    }

    public static EnableStatusEnum ofCode(String code) {
        if (code == null) return null;
        for (EnableStatusEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
