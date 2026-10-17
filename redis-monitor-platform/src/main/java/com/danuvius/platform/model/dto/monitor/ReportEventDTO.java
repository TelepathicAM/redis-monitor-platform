package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 日周报事件明细（告警/主从切换合并列表，按时间倒序）。
 * 字段与原方案 eventList 定义一致。
 */
@Data
public class ReportEventDTO implements Serializable {

    /** 事件类型：告警/主从切换 */
    private String eventType;

    /** 节点展示文本：IP:Port（切换事件为切换双方节点） */
    private String nodeDisplay;

    /** 事件描述内容 */
    private String content;

    /** 发生时间 */
    private LocalDateTime happenTime;

    /** 状态：恢复/未恢复(告警)、成功/失败（主从切换） */
    private String status;
}
