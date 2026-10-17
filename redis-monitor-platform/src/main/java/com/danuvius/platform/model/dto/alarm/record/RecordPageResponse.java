package com.danuvius.platform.model.dto.alarm.record;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 告警记录分页响应 */
@Data
public class RecordPageResponse implements Serializable {
    /*全部分页数*/
    private long total;
    /**/
    private Integer pageNum;
    /*分页大小*/
    private Integer pageSize;

    /*总页数*/
    private Integer totalPages;
    private List<RecordItemResponse> list;
}