package com.danuvius.platform.model.dto.alarm.record;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/** 告警记录分页响应 */
@Data
public class RecordPageResponse implements Serializable {
    /*当前页告警记录列表*/
    private Page<RecordItemResponse> page;
}