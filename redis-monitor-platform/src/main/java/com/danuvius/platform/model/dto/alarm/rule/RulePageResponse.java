package com.danuvius.platform.model.dto.alarm.rule;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 告警规则分页响应
 */
@Data
public class RulePageResponse implements Serializable {
    /*总记录数*/
    private long total;
    /*当前页码*/
    private Integer pageNum;
    /*每页条数*/
    private Integer pageSize;
    /*总页数*/
    private Integer totalPages;
    /*当前页规则列表*/
    private List<RuleItemDTO> list;
}