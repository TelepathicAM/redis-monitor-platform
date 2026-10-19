package com.danuvius.platform.model.dto.audit;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 操作审计列表项 */
@Data
public class AuditLogItemDTO implements Serializable {
    /** 审计日志id */
    private String auditId;
    /** 操作人统一认证号 */
    private String operatorId;
    /** 操作人姓名 */
    private String operatorName;
    /** 功能模块，如：告警规则、报告导出 */
    private String module;
    /** 操作类型，如：新增、修改、删除、恢复、导出 */
    private String operationType;
    /** 操作对象id，如规则id、告警记录id、导出任务id */
    private String targetId;
    /** 操作内容描述 */
    private String operationContent;
    /** 操作结果：成功/失败 */
    private String operationResult;
    /** 操作时间 */
    private LocalDateTime operationTime;
}