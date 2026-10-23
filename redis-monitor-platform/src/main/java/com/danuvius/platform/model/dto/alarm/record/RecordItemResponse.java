package com.danuvius.platform.model.dto.alarm.record;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 告警记录查询
 */
@Data
public class RecordItemResponse implements Serializable {
    /*告警记录id*/
    private String alarmRecordId;
    /*告警编号*/
    private String alarmNo;
    /*关联告警规则id*/
    private String ruleId;
    /*告警触发实例id*/
    private String instanceId;
    /*实例名*/
    private String instanceName;
    /*服务器IP*/
    private String hostIp;
    /*Redis端口*/
    private String redisPort;
    /*监控指标中文名称*/
    private String metricName;
    /*告警级别：1=提示/2=警告/3=严重*/
    private String alarmLevel;
    /*告警描述内容*/
    private String alarmContent;
    /*触发时指标值*/
    private String triggerValue;
    /*告警阈值*/
    private String threshold;
    /*阈值单位*/
    private String unit;
    /*告警触发时间*/
    private LocalDateTime triggerTime;
    /*告警恢复时间，未恢复为 null*/
    private LocalDateTime recoverTime;
    /*告警恢复状态：0=未恢复/1=已恢复*/
    private String recoverStatus;
}
