package com.danuvius.platform.model.dto.alarm.record;

import lombok.Data;

import java.io.Serializable;

/** 告警记录概览统计 */
@Data
public class RecordOverviewResponse implements Serializable {
    private Long alarmTotal;
    private Long unrecoveredCount;
    private Long recoveredCount;
    private Long infoCount;
    private Long alarmCount;
    private Long severityCount;
}