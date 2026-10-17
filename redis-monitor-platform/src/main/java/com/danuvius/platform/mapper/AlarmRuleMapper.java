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
    List<RuleItemDTO> selectRulePage(@Param("keyword") String keyword,
                                     @Param("alarmLevel") String alarmLevel,
                                     @Param("scopeInstanceIds") List<String> scopeInstanceIds,
                                     @Param("enableStatus") String enableStatus,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);

    long countRulePage(@Param("keyword") String keyword,
                       @Param("alarmLevel") String alarmLevel,
                       @Param("scopeInstanceIds") List<String> scopeInstanceIds,
                       @Param("enableStatus") String enableStatus);
    List<AlarmRule> selectAllEnabled();
    RuleOverviewDTO selectRuleOverview();

    /**
     * 查询启用中的告警规则。
     * enable_status = 1  del_flag = 0。
     */
    List<AlarmRule> selectEnabledRules();

    /**
     * 查询启用中的、适用于指定实例的告警规则。
     *   enable_status = 1 del_flag = 0 且（scope_type = 1 全局规则 或 scope_instance_id 包含该实例）
     */
    List<AlarmRule> selectEnabledRulesByInstance(@Param("instanceId") String instanceId);

    /*按指标查询启用中的规则，用于新增规则时的重复校验 */
    List<AlarmRule> selectEnabledRulesByMetric(@Param("metricKey") String metricKey);


}