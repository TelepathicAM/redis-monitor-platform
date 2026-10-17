package com.danuvius.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.danuvius.platform.mapper.SentinelNodeInfoMapper;
import com.danuvius.platform.model.dto.monitor.SentinelItemResponse;
import com.danuvius.platform.model.entity.SentinelNodeInfo;
import com.danuvius.platform.service.SentinelNodeInfoService;
import com.danuvius.platform.util.CacheKeyUtil;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SentinelNodeInfoServiceImpl extends ServiceImpl<SentinelNodeInfoMapper, SentinelNodeInfo>
        implements SentinelNodeInfoService {

    /** 在线状态 */
    private static final String STATUS_ONLINE = "在线";
    /** 离线状态 */
    private static final String STATUS_OFFLINE = "离线";

    /* 哨兵离线判定阈值：超过该时长无心跳视为离线 */
    @Value("${platform.instance.offline-threshold-seconds:600}")
    private long offlineThresholdSeconds;

    private final SentinelNodeInfoMapper sentinelMapper;
    private final Cache<String, Object> caffeineCache;

    public SentinelNodeInfoServiceImpl(SentinelNodeInfoMapper sentinelMapper, Cache<String, Object> caffeineCache) {
        this.sentinelMapper = sentinelMapper;
        this.caffeineCache = caffeineCache;
    }

    /**
     * Sentinel 节点列表。
     * 在线状态判定：有心跳且未超时判为在线，无心跳或心跳超时判为离线。
     */
    @Override
    public List<SentinelItemResponse> getSentinelList(String managementStatus) {
        String key = CacheKeyUtil.sentinelList(managementStatus);
        Object cached = caffeineCache.getIfPresent(key);
        if (cached != null) {
            return (List<SentinelItemResponse>) cached;
        }

        //查询 Sentinel 节点列表
        List<SentinelNodeInfo> list = sentinelMapper.selectSentinelList(managementStatus);

        LocalDateTime offlineBefore = LocalDateTime.now().minusSeconds(offlineThresholdSeconds);

        List<SentinelItemResponse> result = new ArrayList<>();
        if (list != null) {
            for (SentinelNodeInfo s : list) {
                SentinelItemResponse m = new SentinelItemResponse();
                m.setSentinelId(s.getId());
                m.setSentinelName(s.getNodeName());
                m.setSentinelIp(s.getNodeIp());
                m.setSentinelPort(s.getNodePort());
                // 有心跳且未超时，在线；无心跳或超时，离线
                if (s.getLastHeartbeatTime() == null || s.getLastHeartbeatTime().isBefore(offlineBefore)) {
                    m.setSentinelStatus(STATUS_OFFLINE);
                } else {
                    m.setSentinelStatus(STATUS_ONLINE);
                }
                m.setLastHeartbeatTime(s.getLastHeartbeatTime());
                result.add(m);
            }
        }

        caffeineCache.put(key, result);
        return result;
    }


}
