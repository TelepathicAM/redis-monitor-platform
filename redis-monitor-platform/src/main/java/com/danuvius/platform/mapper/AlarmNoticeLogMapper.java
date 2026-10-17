package com.danuvius.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.danuvius.platform.model.entity.AlarmNoticeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 告警通知记录 Mapper
 */
@Mapper
public interface AlarmNoticeLogMapper extends BaseMapper<AlarmNoticeLog> {

    /** 查询某条告警的全部通知记录，按发送时间倒序 */
    List<AlarmNoticeLog> selectByAlarmRecordId(@Param("alarmRecordId") String alarmRecordId);
}
