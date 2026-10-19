package com.danuvius.platform.controller;

import com.danuvius.platform.common.BaseResponse;
import com.danuvius.platform.common.ResultUtils;
import com.danuvius.platform.common.exception.validator.DateValidator;
import com.danuvius.platform.common.exception.validator.NumberValidator;
import com.danuvius.platform.common.exception.validator.StringValidator;
import com.danuvius.platform.model.dto.audit.AuditLogPageRequest;
import com.danuvius.platform.model.dto.audit.AuditLogPageResponse;
import com.danuvius.platform.service.AuditLogService;
import org.apache.commons.lang3.StringUtils;
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
        NumberValidator.between(req.getPageNum(), 1, 10000, "pageNum");
        NumberValidator.between(req.getPageSize(), 1, 100, "pageSize");
        if (StringUtils.isNotBlank(req.getModule())) {
            StringValidator.maxLength(req.getModule(), 32, "module");
        }
        if (StringUtils.isNotBlank(req.getOperationType())) {
            StringValidator.maxLength(req.getOperationType(), 32, "operationType");
        }
        if (StringUtils.isNotBlank(req.getKeyword())) {
            StringValidator.maxLength(req.getKeyword(), 128, "keyword");
        }
        DateValidator.stringRange(req.getStartTime(), req.getEndTime(), "startTime", "endTime");

        return ResultUtils.success(auditLogService.getAuditLogPage(req));
    }
}
