package com.danuvius.platform.model.dto.audit;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.danuvius.platform.model.entity.AuditLog;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**操作审计分页响应 */
@Data
public class AuditLogPageResponse implements Serializable {
    /** 分页结果 */
    private Page<AuditLog> page;
}