package com.danuvius.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.danuvius.platform.model.dto.monitor.RedisInstanceItemDTO;
import com.danuvius.platform.model.entity.RedisInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface RedisInstanceMapper extends BaseMapper<RedisInstance> {

    /** 查询实例列表 */
    List<RedisInstanceItemDTO> selectInstanceList(@Param("managementStatus") String managementStatus);

    /** 查询最新一条指标 */
    List<Map<String, Object>> selectLatestMetric(@Param("instanceId") String instanceId);

    /** 查询上一条指标，用于环比 */
    List<Map<String, Object>> selectPrevMetric(@Param("instanceId") String instanceId);

    /**
     * 查询趋势数据
     * @param instanceIds
     * @param metricKey
     * @param bucketMinutes 分桶分钟数:近1h=5，近6h=15
     * @param dualBandwidth 是否带宽双序列模式
     * @param startTime
     * @param endTime
     * @return
     */
    List<Map<String, Object>> selectTrendData(@Param("instanceIds") List<String> instanceIds,
                                              @Param("metricKey") String metricKey,
                                              @Param("bucketMinutes") int bucketMinutes,
                                              @Param("dualBandwidth") boolean dualBandwidth,
                                              @Param("startTime") String startTime,
                                              @Param("endTime") String endTime);

    /* 日周报概览统计：4项指标均值/峰值/峰值时间 + 采集点数 */
    List<Map<String, Object>> selectReportOverviewStats(@Param("instanceId") String instanceId,
                                                        @Param("startTime") String startTime,
                                                        @Param("endTime") String endTime);

    /* 日周报4项指标趋势序列,当天按小时聚合，近7天按天聚合 */
    List<Map<String, Object>> selectReportTrend(@Param("instanceId") String instanceId,
                                                @Param("startTime") String startTime,
                                                @Param("endTime") String endTime,
                                                @Param("bucketMinutes") int bucketMinutes,
                                                @Param("truncateDay") boolean truncateDay);

    RedisInstance selectByInstanceId(@Param("instanceId") String instanceId);

    /** 查询纳管中的实例,用于拓扑 */
    List<RedisInstance> selectActiveInstances(@Param("managementStatus") String managementStatus);

    /** 查询纳管中的实例，按名称排序,用于表格, */
    List<RedisInstance> selectActiveInstancesOrderByName(@Param("managementStatus") String managementStatus);

    /* 按实例id批量查询实例,用于规则生效节点名称回显 */
    List<RedisInstance> selectInstancesByIds(@Param("instanceIds") List<String> instanceIds);



    /* 更新实例心跳时间与运行状态,采集上报/心跳时维护 */
    int updateHeartbeatAndStatus(@Param("instanceId") String instanceId,
                                 @Param("lastHeartbeatTime") LocalDateTime lastHeartbeatTime,
                                 @Param("runStatus") String runStatus);


}