package com.danuvius.platform.model.dto.metric;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/** Agent 上报的指标项 */
@Data
public class MetricReportItemDTO implements Serializable {
    /** 平台内部回填，Agent 不需要上报 */
    @JsonIgnore
    private String instanceId;

    private String token;
    private String cpuPer;
    private String memServerPer;
    private String memUs;
    private String latency;
    private String ioIik;
    private String ioIok;
    private String ops;
    private String role;
    private String redisVersion;
    private String runId;
    private String masterHost;
    private String masterPort;
    private String replDelay;


    /** 采集类型：0=自动，1=手动 */
    private String collectType;

    /** 连通性延迟，执行器端采集时上报 */
    private String pingLatency;
    private String errorMsg;
}