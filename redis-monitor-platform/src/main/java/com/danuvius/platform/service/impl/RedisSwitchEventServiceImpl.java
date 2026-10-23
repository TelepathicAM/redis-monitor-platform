package com.danuvius.platform.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.danuvius.platform.mapper.RedisSwitchEventMapper;
import com.danuvius.platform.model.entity.RedisSwitchEvent;
import com.danuvius.platform.service.RedisSwitchEventService;
import org.springframework.stereotype.Service;

@Service
public class RedisSwitchEventServiceImpl extends ServiceImpl<RedisSwitchEventMapper, RedisSwitchEvent>
        implements RedisSwitchEventService {
    private final RedisSwitchEventMapper redisSwitchEventMapper;
    public RedisSwitchEventServiceImpl(RedisSwitchEventMapper redisSwitchEventMapper) {
        this.redisSwitchEventMapper = redisSwitchEventMapper;
    }
    public RedisSwitchEvent selectLatestSwitchEvent(){
        RedisSwitchEvent redisSwitchEvent = redisSwitchEventMapper.selectOne(Wrappers.<RedisSwitchEvent>lambdaQuery()
                .orderByDesc(RedisSwitchEvent::getSwitchTime)
                .last("limit 1")
        );
        return redisSwitchEvent;
    }
}
