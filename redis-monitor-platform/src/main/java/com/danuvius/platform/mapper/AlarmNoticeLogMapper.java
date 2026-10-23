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


}
