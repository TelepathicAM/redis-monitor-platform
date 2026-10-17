package com.danuvius.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.danuvius.platform.model.entity.AlarmNoticeLog;
import com.danuvius.platform.model.entity.AlarmRecord;
import com.danuvius.platform.model.entity.AlarmRule;

import java.util.List;

/**
 * 告警通知 Service。
 * 对应原方案"每轮监控采集后根据告警规则自动比对实时指标值，
 * 符合条件生成告警记录并邮件通知"。
 */
public interface AlarmNoticeService extends IService<AlarmNoticeLog> {

    /**
     * 告警生成后发送邮件通知并记录通知结果。
     * 通知失败不影响告警主流程。
     *
     * @param record 告警记录
     * @param rule   触发告警的规则（用于邮件内容展示）
     */
    void sendAlarmNotice(AlarmRecord record, AlarmRule rule);

    /**
     * 查询某条告警的通知记录。
     */
    List<AlarmNoticeLog> listByAlarmRecordId(String alarmRecordId);
}
