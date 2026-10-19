package com.danuvius.platform.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作审计日志实体。
 */
@Data
@TableName("audit_log")
public class AuditLog {
    /*主键id*/
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /*操作人统一认证号，无认证体系时默认 system*/
    private String operatorId;

    /*操作人姓名*/
    private String operatorName;

    /*功能模块，如：告警规则、报告导出*/
    private String module;

    /*操作类型，如：新增、修改、删除、导出*/
    private String operationType;

    /*操作对象id，如规则id、导出任务id*/
    private String targetId;

    /*操作内容描述*/
    private String operationContent;

    /*操作结果：成功/失败*/
    private String operationResult;

    /*操作时间*/
    private LocalDateTime operationTime;

    /*创建时间*/
    private LocalDateTime createdTime;
}
