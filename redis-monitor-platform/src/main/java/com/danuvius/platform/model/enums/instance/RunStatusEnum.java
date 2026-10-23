package com.danuvius.platform.model.enums.instance;

import lombok.Getter;

@Getter
public enum RunStatusEnum {
    /**
     * redis_instance表
     */
    /*runStatus*/
    RUN_OFFLINE("0", "离线"),
    RUN_NORMAL("1", "正常"),
    RUN_WARNING("2", "警告"),
    RUN_CRITICAL("3", "严重");

    private final String code;
    private final String desc;

    RunStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**数字转文字*/
    public static String descOf(String code) {
        if (code == null) {
            return null;
        }
        for (RunStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e.desc;
            }
        }
        // 已是文字的直接返回
        for (RunStatusEnum e : values()) {
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
        for (RunStatusEnum e : values()) {
            if (e.desc.equals(desc)) {
                return e.code;
            }
        }
        for (RunStatusEnum e : values()) {
            if (e.code.equals(desc)) {
                return e.code;
            }
        }
        return desc;
    }

    public static RunStatusEnum ofCode(String code) {
        if (code == null) return null;
        for (RunStatusEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
