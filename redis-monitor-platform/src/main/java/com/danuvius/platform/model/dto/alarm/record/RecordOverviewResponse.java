package com.danuvius.platform.model.dto.alarm.record;

import lombok.Data;

import java.io.Serializable;

/**
 * 告警记录概览统计
 */
@Data
public class RecordOverviewResponse implements Serializable {
    /*告警总数*/
    private Long alarmTotal;
    /*未恢复告警数*/
    private Long unrecoveredCount;
    /*已恢复告警数*/
    private Long recoveredCount;
    /*提示级别告警数*/
    private Long infoCount;
    /*警告级别告警数*/
    private Long alarmCount;
    /*严重级别告警数*/
    private Long severityCount;
}