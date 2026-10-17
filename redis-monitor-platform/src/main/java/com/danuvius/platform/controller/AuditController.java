package com.danuvius.platform.controller;

import com.danuvius.platform.common.BaseResponse;
import com.danuvius.platform.common.ResultUtils;
import com.danuvius.platform.model.dto.audit.AuditLogPageRequest;
import com.danuvius.platform.model.dto.audit.AuditLogPageResponse;
import com.danuvius.platform.service.AuditLogService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 查询操作审计列表接口 /api/audit/logs。
 */
@RestController
@RequestMapping("/api/audit")
@Validated
public class AuditController {

    private final AuditLogService auditLogService;

    public AuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * 查询操作审计列表接口。
     * 仪表盘展示最近5条操作记录时传 pageSize=5。
     */
    @GetMapping("/logs")
    public BaseResponse<AuditLogPageResponse> logs(@Valid AuditLogPageRequest req) {
        return ResultUtils.success(auditLogService.getAuditLogPage(req));
    }
}
