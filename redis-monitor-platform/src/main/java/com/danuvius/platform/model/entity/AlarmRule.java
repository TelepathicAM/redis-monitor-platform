package com.danuvius.platform.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("alarm_rule")
public class AlarmRule {
    @TableId(type = IdType.INPUT)
    private String ruleId;
    private String ruleName;
    private String metricKey;
    private String metricName;
    private String compareType;
    private String threshold;
    private String unit;
    private String collectInterval;
    private String duration;
    private String alarmLevel;
    private String silenceDuration;
    private String scopeType;
    private String scopeInstanceId;
    private String enableStatus;
    private String delFlag;
    private String createdBy;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
