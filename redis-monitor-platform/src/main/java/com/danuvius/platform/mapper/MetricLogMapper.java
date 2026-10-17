package com.danuvius.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.danuvius.platform.model.entity.MetricLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface MetricLogMapper extends BaseMapper<MetricLog> {
    /*清除7天前记录*/
    int deleteExpiredBefore(@Param("threshold") LocalDateTime threshold);

    /*按实例取最新一条指标，拓扑/节点状态判断*/
    MetricLog selectLatestByInstanceId(@Param("instanceId") String instanceId);

    /**
     * 查询某个实例某个指标在指定时间段内的采集时间序列。
     */
    List<Map<String, Object>> selectMetricTrend(@Param("instanceId") String instanceId,
                                                @Param("metricKey") String metricKey,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 取某个实例某个指标的最近 N 条采集值（用于持续时长判断），倒序。
     */
    List<String> selectRecentMetricValues(@Param("instanceId") String instanceId,
                                          @Param("metricKey") String metricKey,
                                          @Param("limit") int limit);

}
