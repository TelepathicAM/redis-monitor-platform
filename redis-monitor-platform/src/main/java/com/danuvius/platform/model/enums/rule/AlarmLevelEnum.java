package com.danuvius.platform.model.enums.rule;

import lombok.Getter;

/**
 * 告警级别 / 指标状态。
 * 正常、提示、警告、严重用于告警级别；
 * 无数据用于指标实时状态展示，不属于真实告警级别。
 * 数据库存储为数字字符串，展示时映射为中文说明。
 */
@Getter
public enum AlarmLevelEnum {

    NO_DATA("-1", "无数据"),
    NORMAL("0", "正常"),
    INFO("1", "提示"),
    WARNING("2", "警告"),
    CRITICAL("3", "严重");

    /**级别数值*/
    private final String code;

    /**中文描述*/
    private final String desc;

    AlarmLevelEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    /**数字转文字*/
    public static String descOf(String code) {
        if (code == null) {
            return null;
        }
        for (AlarmLevelEnum e : values()) {
            if (e.code.equals(code)) {
                return e.desc;
            }
        }
        // 已是文字的直接返回
        for (AlarmLevelEnum e : values()) {
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
        for (AlarmLevelEnum e : values()) {
            if (e.desc.equals(desc)) {
                return e.code;
            }
        }
        for (AlarmLevelEnum e : values()) {
            if (e.code.equals(desc)) {
                return e.code;
            }
        }
        return desc;
    }
}