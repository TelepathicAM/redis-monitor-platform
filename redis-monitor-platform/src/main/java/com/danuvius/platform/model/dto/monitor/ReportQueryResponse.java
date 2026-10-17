package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 日周报查询响应。
 */
@Data
public class ReportQueryResponse implements Serializable {

    /** 8+项概览统计卡片数据 */
    private ReportOverviewDTO overview;

    /** 4项指标趋势序列数组 */
    private List<ReportTrendDTO> trendList;

    /** 告警/主从切换事件合并列表，按时间倒序 */
    private List<ReportEventDTO> eventList;
}
