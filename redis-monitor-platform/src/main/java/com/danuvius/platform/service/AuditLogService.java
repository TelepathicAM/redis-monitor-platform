package com.danuvius.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.danuvius.platform.model.dto.audit.AuditLogPageRequest;
import com.danuvius.platform.model.dto.audit.AuditLogPageResponse;
import com.danuvius.platform.model.entity.AuditLog;

/**
 * 操作审计日志 Service。
 * 原方案要求：规则新增/修改/删除/切换、报告导出等操作均记入操作审计日志，
 * 监控仪表盘通过 /api/audit/logs 展示最近操作记录。
 */
public interface AuditLogService extends IService<AuditLog> {

    /**
     * 记录一条审计日志，操作人从请求头 X-Operator-Id / X-Operator-Name 获取，默认 system。
     *
     * @param module        功能模块
     * @param operationType 操作类型
     * @param targetId      操作对象id
     * @param content       操作内容描述
     */
    void record(String module, String operationType, String targetId, String content);

    /**
     * 记录一条审计日志（显式指定操作人与结果）。
     */
    void record(String operatorId, String operatorName, String module, String operationType,
                String targetId, String content, String operationResult);

    /**
     * 分页查询审计日志。
     */
    AuditLogPageResponse getAuditLogPage(AuditLogPageRequest req);
}
