package com.danuvius.platform.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("rms_metric_log")
public class MetricLog {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String instanceId;
    private LocalDateTime tm;

    /** CPU 占用率（%） */
    private String cpuPer;
    /** 服务器内存占用率（%） */
    private String memServerPer;
    /** Redis 内存使用量（字节） */
    private String memUs;
    /** 命令加权平均延迟（微秒） */
    private String latency;
    /** 网络输入带宽（kbps） */
    private String ioIik;
    /** 网络输出带宽（kbps） */
    private String ioIok;
    /** 瞬时 QPS(次/秒) */
    private String ops;
    private String replDelay;

    private String runId;
    private String masterHost;
    private String masterPort;
    private String role;
    private String redisVersion;

    /** 采集类型 */
    private String collectType;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
