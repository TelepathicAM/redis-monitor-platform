package com.danuvius.platform.model.dto.audit;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**操作审计分页响应 */
@Data
public class AuditLogPageResponse implements Serializable {
    private long total;
    private Integer pageNum;
    private Integer pageSize;
    private Integer totalPages;
    private List<AuditLogItemDTO> list;
}
