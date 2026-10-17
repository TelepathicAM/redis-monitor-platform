package com.danuvius.platform.model.enums;

import lombok.Getter;

@Getter
public enum TypeEnum {
    /**
     * redis_instance表
     */
    /*role*/
    MASTER("0","master"),
    REPLICA("1","replica"),
    UNKNOWN("2","unknown"),

    /**
     * audit_Log表
     */
    /*module*/
    REPORT_MODULE("0","report"),       //报告模块
    RULE_MODULE("1","rule"),           //告警规则模块
    RECORD_MODULE("3","record"),       //告警记录模块
    OPERATION_MODULE("4","operation"), //运维任务模块
    INSTANCE_MODULE("5","instance"),   //实例管理模块

    /*operationType*/
    EXPORT_MODULLE("0","export"),
    RULE_ADD("1","add"),
    RULE_DELETE("2","delete"),
    RULE_UPDATE("3","update"),
    OPERATION_FAULT_RECOVERY("4","fault_recovery"),
    OPERATION__VERSION_UPGRADE("5","version_upgrade"),
    OPERATION_SWITCH_SERVER("6","switch_server"),

    REDIS_REGISTER("7","redis_register"),
    REDIS_UPDATE("8","redis_update"),
    REDIS_DELETE("9","redis_delete"),

    /**
     * alarm_rule表
     */
    /*scopeType*/
    ALL_INSTANCE_IDS("0","all_instance_ids"),
    SPECIFIED_INSTNACE_ID("1","specified_instnace_id"),
    /*delFlag*/
    NOT_DELETED("1","not_deleted"),
    DELETED("1","deleted"),


    /**
     * redis_switch_event表
     */
    /*switchType*/
    INIT("0", "基线"),
    AUTO("1", "自动切换"),
    MANUAL("2", "手动切换"),
    OTHER("3", "其他"),

    /**
     * rms_metric_log表
      */
    /*collectType*/
    AUTO_COLLECT("0","auto_collect"),
    MANUAL_COLLECT("1","manual_collect"),

    /*日周报报告，告警与主从切换统计前端表格类型项*/
    ALARM("alarm","告警"),
    MASTER_SWITCH("masterSwitch","主从切换");




    private final String code;
    private final String desc;

    TypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**数字转文字*/
    public static String descOf(String code) {
        if (code == null) {
            return null;
        }
        for (TypeEnum e : values()) {
            if (e.code.equals(code)) {
                return e.desc;
            }
        }
        // 已是文字的直接返回
        for (TypeEnum e : values()) {
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
        for (TypeEnum e : values()) {
            if (e.desc.equals(desc)) {
                return e.code;
            }
        }
        for (TypeEnum e : values()) {
            if (e.code.equals(desc)) {
                return e.code;
            }
        }
        return desc;
    }

    public static TypeEnum ofCode(String code) {
        if (code == null) return null;
        for (TypeEnum e : values()) {
            if (e.code.equals(code)) return e;
        }
        return null;
    }

}
