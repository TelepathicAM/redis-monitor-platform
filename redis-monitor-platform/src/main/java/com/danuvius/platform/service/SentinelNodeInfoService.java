package com.danuvius.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.danuvius.platform.model.dto.monitor.SentinelItemResponse;
import com.danuvius.platform.model.entity.SentinelNodeInfo;

import java.util.List;

public interface SentinelNodeInfoService extends IService<SentinelNodeInfo> {
    /**
     * Sentinel列表
     * @param managementStatus
     * @return
     */
    List<SentinelItemResponse> getSentinelList(String managementStatus);




}
