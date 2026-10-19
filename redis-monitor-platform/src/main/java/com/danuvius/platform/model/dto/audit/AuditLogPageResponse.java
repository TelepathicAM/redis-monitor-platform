package com.danuvius.platform.model.dto.audit;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**操作审计分页响应 */
@Data
public class AuditLogPageResponse implements Serializable {
    /** 总记录数 */
    private long total;
    /** 当前页码 */
    private Integer pageNum;
    /** 每页条数 */
    private Integer pageSize;
    /** 总页数 */
    private Integer totalPages;
    /** 当前页审计日志列表 */
    private List<AuditLogItemDTO> list;
}