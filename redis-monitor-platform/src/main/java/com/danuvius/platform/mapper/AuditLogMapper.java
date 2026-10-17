package com.danuvius.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.danuvius.platform.model.dto.audit.AuditLogItemDTO;
import com.danuvius.platform.model.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作审计日志 Mapper
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {

    /** 分页查询审计日志 */
    List<AuditLogItemDTO> selectAuditLogPage(@Param("module") String module,
                                             @Param("operationType") String operationType,
                                             @Param("keyword") String keyword,
                                             @Param("startTime") String startTime,
                                             @Param("endTime") String endTime,
                                             @Param("offset") int offset,
                                             @Param("limit") int limit);

    /** 统计审计日志总数 */
    long countAuditLogPage(@Param("module") String module,
                           @Param("operationType") String operationType,
                           @Param("keyword") String keyword,
                           @Param("startTime") String startTime,
                           @Param("endTime") String endTime);
}
