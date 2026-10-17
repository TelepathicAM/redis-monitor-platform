package com.danuvius.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.danuvius.platform.model.entity.NodeStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface NodeStatusMapper extends BaseMapper<NodeStatus> {
    int deleteExpiredBefore(@Param("threshold") LocalDateTime threshold);
}
