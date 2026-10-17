package com.danuvius.platform.model.dto.alarm.rule;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 告警规则分页响应 */
@Data
public class RulePageResponse implements Serializable {
    private long total;
    private Integer pageNum;
    private Integer pageSize;
    private Integer totalPages;
    private List<RuleItemDTO> list;
}