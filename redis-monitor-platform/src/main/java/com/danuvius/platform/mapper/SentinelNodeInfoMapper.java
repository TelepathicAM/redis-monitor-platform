package com.danuvius.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.danuvius.platform.model.entity.SentinelNodeInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface SentinelNodeInfoMapper extends BaseMapper<SentinelNodeInfo> {
    /**
     * 查询 Sentinel 节点列表。
     * managementStatus 为 null 时查询全部；否则按纳管状态过滤。
     */
    List<SentinelNodeInfo> selectSentinelList(@Param("managementStatus") String managementStatus);
}
