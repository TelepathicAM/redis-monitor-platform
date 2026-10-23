package com.danuvius.platform.model.enums.rule;

import lombok.Getter;

@Getter
public enum DelFlagEnum {
    /**
     * alarm_rule表
     */
    /*delFlag*/
    NOT_DELETED("0","not_deleted"),
    DELETED("1","deleted");

    private final String code;
    private final String desc;

    DelFlagEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**数字转文字*/
    public static String descOf(String code) {
        if (code == null) {
            return null;
        }
        for (DelFlagEnum e : values()) {
            if (e.code.equals(code)) {
                return e.desc;
            }
        }
        // 已是文字的直接返回
        for (DelFlagEnum e : values()) {
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
        for (DelFlagEnum e : values()) {
            if (e.desc.equals(desc)) {
                return e.code;
            }
        }
        for (DelFlagEnum e : values()) {
            if (e.code.equals(desc)) {
                return e.code;
            }
        }
        return desc;
    }

    public static DelFlagEnum ofCode(String code) {
        if (code == null) return null;
        for (DelFlagEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
    
    
}
