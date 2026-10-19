package com.danuvius.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.danuvius.platform.model.dto.alarm.record.*;
import com.danuvius.platform.model.entity.AlarmRecord;

import java.util.List;

public interface AlarmRecordService extends IService<AlarmRecord> {
    /**
     * 最近未恢复
     * @return
     */
    List<RecordItemResponse> getRecentUnrecovered();

    /**
     * 最近记录
     * @return
     */
    List<RecordItemResponse> getRecent();

    /**
     * 记录概览
     * @param startTime
     * @param endTime
     * @return
     */
    RecordOverviewResponse getRecordOverview(String startTime, String endTime);

    /**
     * 记录查询分页
     * @param recordPageRequest
     * @return
     */
    RecordPageResponse getRecordPage(RecordPageRequest recordPageRequest);

    /**
     * 记录详情
     * @param alarmRecordId
     * @return
     */
    RecordDetailResponse getRecordDetail(String alarmRecordId);

    /**
     * 详情曲线
     * @param alarmRecordId
     * @return
     */
    RecordMetricTrendResponse getMetricTrend(String alarmRecordId);

    /**
     * 告警人工确认恢复
     * @param alarmRecordId
     * @return
     */
    boolean recoverAlarm(String alarmRecordId);

}
