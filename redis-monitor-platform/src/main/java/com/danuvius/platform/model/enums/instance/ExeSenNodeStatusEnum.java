package com.danuvius.platform.model.enums.instance;


public enum ExeSenNodeStatusEnum {
    /**
     * executor_info表 / sentinel_node_info表 / rms_node_status_log表
     */
    /*executorStatus / sentinelStatus / collectStatus*/
    OFFLINE("0","离线"),
    ONLINE("1","在线");



    private final String code;
    private final String desc;

    ExeSenNodeStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**数字转文字*/
    public static String descOf(String code) {
        if (code == null) {
            return null;
        }
        for (ExeSenNodeStatusEnum e : values()) {
            if (e.code.equals(code)) {
                return e.desc;
            }
        }
        // 已是文字的直接返回
        for (ExeSenNodeStatusEnum e : values()) {
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
        for (ExeSenNodeStatusEnum e : values()) {
            if (e.desc.equals(desc)) {
                return e.code;
            }
        }
        for (ExeSenNodeStatusEnum e : values()) {
            if (e.code.equals(desc)) {
                return e.code;
            }
        }
        return desc;
    }

    public static ExeSenNodeStatusEnum ofCode(String code) {
        if (code == null) return null;
        for (ExeSenNodeStatusEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }
}
