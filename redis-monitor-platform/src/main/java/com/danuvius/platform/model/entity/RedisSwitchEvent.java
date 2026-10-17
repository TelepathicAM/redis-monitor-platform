package com.danuvius.platform.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("redis_switch_event")
public class RedisSwitchEvent {
    @TableId(type = IdType.INPUT)
    private String id;
    private String oldMasterId;
    private String newMasterId;
    private LocalDateTime switchTime;
    private String switchType;
    private String switchResult;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
