package com.danuvius.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.danuvius.platform.model.dto.alarm.rule.RuleItemDTO;
import com.danuvius.platform.model.dto.alarm.rule.RuleOverviewDTO;
import com.danuvius.platform.model.entity.AlarmRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface AlarmRuleMapper extends BaseMapper<AlarmRule> {

    RuleOverviewDTO selectRuleOverview();





}