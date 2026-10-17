package com.danuvius.platform.model.dto.audit;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 操作审计列表项 */
@Data
public class AuditLogItemDTO implements Serializable {
    private String auditId;
    private String operatorId;
    private String operatorName;
    private String module;
    private String operationType;
    private String targetId;
    private String operationContent;
    private String operationResult;
    private LocalDateTime operationTime;
}
