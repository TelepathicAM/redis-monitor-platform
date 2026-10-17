package com.danuvius.platform.model.dto.alarm.rule;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
/*告警列表查询请求*/
@Data
public class RuleListRequest implements Serializable {
    private String keyword;
    private String alarmLevel;
    private List<String> scopeInstanceIds;
    private String enableStatus;
    private Integer pageNum;
    private Integer pageSize;
}
