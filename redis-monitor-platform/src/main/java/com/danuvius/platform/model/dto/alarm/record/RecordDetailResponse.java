package com.danuvius.platform.model.dto.alarm.record;

import com.danuvius.platform.model.entity.AlarmNoticeLog;
import com.danuvius.platform.model.entity.AlarmRecord;
import com.danuvius.platform.model.entity.AlarmRule;
import com.danuvius.platform.model.entity.RedisInstance;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 告警记录详情 */
@Data
public class RecordDetailResponse implements Serializable {
    private AlarmRecord alarmBaseInfo;
    private AlarmRule ruleInfo;

    /* 告警实例信息（实例名/IP/端口/角色） */
    private RedisInstance instanceInfo;

    /* 通知发送记录（邮件发送时间/接收人/发送状态） */
    private List<AlarmNoticeLog> noticeLogs;

    /* 处理人信息（当前无处理流程时为空） */
    private String operatorId;
    private String operatorName;
    private String operationTime;
    private String operationMsg;
}