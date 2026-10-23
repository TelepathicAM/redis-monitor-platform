package com.danuvius.platform.model.enums.rule;

import lombok.Getter;

@Getter
public enum ScopeTypeEnum {
    /**
     * alarm_rule表
     */
    /*scopeType*/
    ALL_INSTANCE_IDS("1","all_instance_ids"),
    SPECIFIED_INSTNACE_ID("2","specified_instnace_id");

    private final String code;
    private final String desc;

    ScopeTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**数字转文字*/
    public static String descOf(String code) {
        if (code == null) {
            return null;
        }
        for (ScopeTypeEnum e : values()) {
            if (e.code.equals(code)) {
                return e.desc;
            }
        }
        // 已是文字的直接返回
        for (ScopeTypeEnum e : values()) {
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
        for (ScopeTypeEnum e : values()) {
            if (e.desc.equals(desc)) {
                return e.code;
            }
        }
        for (ScopeTypeEnum e : values()) {
            if (e.code.equals(desc)) {
                return e.code;
            }
        }
        return desc;
    }

    public static ScopeTypeEnum ofCode(String code) {
        if (code == null) return null;
        for (ScopeTypeEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
