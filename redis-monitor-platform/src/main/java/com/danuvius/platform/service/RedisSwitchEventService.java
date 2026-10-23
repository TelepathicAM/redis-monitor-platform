package com.danuvius.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.danuvius.platform.model.entity.RedisSwitchEvent;

public interface RedisSwitchEventService extends IService<RedisSwitchEvent>{
    RedisSwitchEvent selectLatestSwitchEvent();

}
