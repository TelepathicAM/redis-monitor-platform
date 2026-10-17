package com.danuvius.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.danuvius.platform.mapper.AuditLogMapper;
import com.danuvius.platform.model.dto.audit.AuditLogItemDTO;
import com.danuvius.platform.model.dto.audit.AuditLogPageRequest;
import com.danuvius.platform.model.dto.audit.AuditLogPageResponse;
import com.danuvius.platform.model.entity.AuditLog;
import com.danuvius.platform.service.AuditLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 操作审计日志
 */
@Slf4j
@Service
public class AuditLogServiceImpl extends ServiceImpl<AuditLogMapper, AuditLog>
        implements AuditLogService {

    /** 默认操作人（当前平台无登录认证体系，可由请求头覆盖） */
    private static final String DEFAULT_OPERATOR_ID = "system";
    private static final String DEFAULT_OPERATOR_NAME = "系统";

    private final AuditLogMapper auditLogMapper;

    public AuditLogServiceImpl(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    public void record(String module, String operationType, String targetId, String content) {
        String operatorId = DEFAULT_OPERATOR_ID;
        String operatorName = DEFAULT_OPERATOR_NAME;
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String headerId = request.getHeader("X-Operator-Id");
                String headerName = request.getHeader("X-Operator-Name");
                if (headerId != null && !headerId.trim().isEmpty()) {
                    operatorId = headerId;
                }
                if (headerName != null && !headerName.trim().isEmpty()) {
                    operatorName = headerName;
                }
            }
        } catch (Exception e) {
            log.warn("[操作审计] 获取操作人信息失败，使用默认值", e);
        }
        record(operatorId, operatorName, module, operationType, targetId, content, "成功");
    }

    @Override
    public void record(String operatorId, String operatorName, String module, String operationType,
                       String targetId, String content, String operationResult) {
        try {
            LocalDateTime now = LocalDateTime.now();
            AuditLog auditLog = new AuditLog();
            auditLog.setId(UUID.randomUUID().toString().replace("-", ""));
            auditLog.setOperatorId(operatorId);
            auditLog.setOperatorName(operatorName);
            auditLog.setModule(module);
            auditLog.setOperationType(operationType);
            auditLog.setTargetId(targetId);
            auditLog.setOperationContent(content);
            auditLog.setOperationResult(operationResult);
            auditLog.setOperationTime(now);
            auditLog.setCreatedTime(now);
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            // 审计记录失败不能影响主业务流程
            log.error("[操作审计] 记录失败, module={}, operationType={}", module, operationType, e);
        }
    }

    @Override
    public AuditLogPageResponse getAuditLogPage(AuditLogPageRequest req) {
        int pageNum = req.getPageNum() == null || req.getPageNum() < 1 ? 1 : req.getPageNum();
        int pageSize = req.getPageSize() == null || req.getPageSize() < 1 ? 12 : req.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        List<AuditLogItemDTO> list = auditLogMapper.selectAuditLogPage(
                req.getModule(), req.getOperationType(), req.getKeyword(),
                req.getStartTime(), req.getEndTime(), offset, pageSize);
        long total = auditLogMapper.countAuditLogPage(
                req.getModule(), req.getOperationType(), req.getKeyword(),
                req.getStartTime(), req.getEndTime());

        AuditLogPageResponse resp = new AuditLogPageResponse();
        resp.setTotal(total);
        resp.setPageNum(pageNum);
        resp.setPageSize(pageSize);
        resp.setTotalPages((int) Math.ceil((double) total / pageSize));
        resp.setList(list);
        return resp;
    }
}
