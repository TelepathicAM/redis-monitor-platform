package com.danuvius.platform.util;

import com.danuvius.platform.mapper.RedisInstanceMapper;
import com.danuvius.platform.model.entity.AlarmRule;
import com.danuvius.platform.model.entity.RedisInstance;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RuleMatchHelper {

    private final RedisInstanceMapper redisInstanceMapper;

    public RuleMatchHelper(RedisInstanceMapper redisInstanceMapper) {
        this.redisInstanceMapper = redisInstanceMapper;
    }

    /**
     * 解析规则作用的实例 ID 列表：
     * scopeType=1 全局，取所有纳管实例
     * scopeType=2 指定实例，按逗号拆分
     */
    public List<String> resolveRuleInstanceIds(AlarmRule rule) {
        List<String> result = new ArrayList<>();
        if ("1".equals(rule.getScopeType())) {
            List<RedisInstance> all = redisInstanceMapper.selectActiveInstances("1");
            if (all != null) {
                for (RedisInstance i : all) {
                    result.add(i.getInstanceId());
                }
            }
        } else if ("2".equals(rule.getScopeType())) {
            String ids = rule.getScopeInstanceId();
            if (ids != null && !ids.isEmpty()) {
                String[] arr = ids.split(",");
                for (String id : arr) {
                    String t = id.trim();
                    if (!t.isEmpty()) {
                        result.add(t);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 判断规则是否作用到指定实例。
     */
    public boolean isRuleApplyToInstance(AlarmRule rule, String instanceId) {
        if (instanceId == null) {
            return false;
        }
        String scopeType = rule.getScopeType();
        if ("1".equals(scopeType)) {
            return true;
        }
        if ("2".equals(scopeType)) {
            String ids = rule.getScopeInstanceId();
            if (ids == null || ids.isEmpty()) {
                return false;
            }
            String[] arr = ids.split(",");
            for (String id : arr) {
                if (id.trim().equals(instanceId)) {
                    return true;
                }
            }
        }
        return false;
    }
}