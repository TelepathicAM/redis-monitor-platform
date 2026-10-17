package com.danuvius.platform.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.danuvius.platform.common.exception.BusinessCode;
import com.danuvius.platform.common.exception.BusinessException;
import com.danuvius.platform.mapper.AlarmRecordMapper;
import com.danuvius.platform.mapper.AlarmRuleMapper;
import com.danuvius.platform.mapper.MetricLogMapper;
import com.danuvius.platform.mapper.RedisInstanceMapper;
import com.danuvius.platform.model.dto.alarm.record.*;
import com.danuvius.platform.model.entity.AlarmRecord;
import com.danuvius.platform.service.AlarmNoticeService;
import com.danuvius.platform.service.AlarmRecordService;
import com.danuvius.platform.util.ParseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AlarmRecordServiceImpl extends ServiceImpl<AlarmRecordMapper, AlarmRecord>
        implements AlarmRecordService {
    private final AlarmRecordMapper recordMapper;
    private final AlarmRuleMapper ruleMapper;
    private final MetricLogMapper metricLogMapper;

    /*告警详情需关联实例信息与通知记录 */
    private final RedisInstanceMapper redisInstanceMapper;
    private final AlarmNoticeService alarmNoticeService;
    private  final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public AlarmRecordServiceImpl(AlarmRuleMapper ruleMapper, AlarmRecordMapper recordMapper,
                                  MetricLogMapper metricLogMapper, RedisInstanceMapper redisInstanceMapper,
                                  AlarmNoticeService alarmNoticeService) {
        this.recordMapper = recordMapper;
        this.ruleMapper = ruleMapper;
        this.metricLogMapper = metricLogMapper;
        this.redisInstanceMapper = redisInstanceMapper;
        this.alarmNoticeService = alarmNoticeService;
    }
    @Override
    public List<RecordItemResponse> getRecentUnrecovered() {
        List<RecordItemResponse> list = recordMapper.selectRecentUnrecovered(5);

        return CollectionUtils.isEmpty(list) ? new ArrayList<>() : list;
    }


    @Override
    public List<RecordItemResponse> getRecent() {
        List<RecordItemResponse> list = recordMapper.selectRecent(5);

        return CollectionUtils.isEmpty(list) ? new ArrayList<>() : list;

    }

    @Override
    public RecordOverviewResponse getRecordOverview(String startTime, String endTime) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(3);
        if (startTime == null) startTime = start.toLocalDate().atStartOfDay().format(FORMATTER);
        if (endTime == null) endTime = end.format(FORMATTER);
        return recordMapper.selectRecordOverview(startTime, endTime);
    }

    /**
     * 记录分页查询
     * @param req
     * @return
     */
    @Override
    public RecordPageResponse getRecordPage(RecordPageRequest req) {
        int pageNum = req.getPageNum() == null || req.getPageNum() < 1 ? 1 : req.getPageNum();
        int pageSize = req.getPageSize() == null || req.getPageSize() < 1 ? 12 : req.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        String instanceId=req.getInstanceId();
        Integer alarmLevel=req.getAlarmLevel();
        String metricKey=req.getMetricKey();
        String recoverStatus=req.getRecoverStatus();
        String keyword=req.getKeyword();
        String startTime=req.getStartTime();
        String endTime=req.getEndTime();

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(3);
        if (startTime == null) startTime = start.toLocalDate().atStartOfDay().format(FORMATTER);
        if (endTime == null) endTime = end.format(FORMATTER);

        //查
        List<RecordItemResponse> list = recordMapper.selectRecordPage(
                instanceId, alarmLevel, metricKey, recoverStatus, keyword, startTime, endTime,offset,pageNum);
        long total = recordMapper.countRecordPage(
                instanceId, alarmLevel, metricKey, recoverStatus, keyword, startTime, endTime);


        RecordPageResponse resp = new RecordPageResponse();
        resp.setTotal(total);
        resp.setPageNum(pageNum);
        resp.setPageSize(pageSize);
        resp.setTotalPages((int) Math.ceil((double) total / pageSize));
        resp.setList(list);
        return resp;
    }

    /**
     * 记录详情
     * @param alarmRecordId
     * @return
     */
    @Override
    public RecordDetailResponse getRecordDetail(String alarmRecordId) {
        AlarmRecord record = recordMapper.selectById(alarmRecordId);
        if (record == null) throw new BusinessException(BusinessCode.NOT_FOUND_ERROR.getCode(), "告警记录不存在");

        RecordDetailResponse dto = new RecordDetailResponse();
        dto.setAlarmBaseInfo(record);
        if (record.getRuleId() != null) {
            dto.setRuleInfo(ruleMapper.selectById(record.getRuleId()));
        }
        /* 关联节点信息表查询实例名、ip、端口、角色 */
        if (record.getInstanceId() != null) {
            dto.setInstanceInfo(redisInstanceMapper.selectById(record.getInstanceId()));
        }
        /* 查询通知发送记录表，返回邮件通知发送时间、接收人、发送状态 */
        dto.setNoticeLogs(alarmNoticeService.listByAlarmRecordId(record.getId()));
        return dto;
    }

    /**
     * 详情曲线
     * @param alarmRecordId
     * @return
     */
    @Override
    public RecordMetricTrendResponse getMetricTrend(String alarmRecordId) {
        AlarmRecord record = recordMapper.selectById(alarmRecordId);
        if (record == null) throw new BusinessException(BusinessCode.NOT_FOUND_ERROR.getCode(), "告警记录不存在");

        RecordMetricTrendResponse dto = new RecordMetricTrendResponse();
        dto.setMetricName(record.getMetricName());
        dto.setThresholdValue(record.getThreshold());
        dto.setTriggerTime(record.getTriggerTime());
        dto.setRecoverTime(record.getRecoverTime());
        dto.setTimestampList(new ArrayList<>());
        dto.setValueList(new ArrayList<>());

        if (record.getTriggerTime() == null) {
            return dto;
        }

        //曲线序列开始时间：触发时间-30min
        LocalDateTime start = record.getTriggerTime().minusMinutes(30);
        LocalDateTime end;
        //未恢复：当前时间，已恢复：恢复时间+10min
        if (Boolean.TRUE.equals(record.getRecoverStatus()) && record.getRecoverTime() != null) {
            end = record.getRecoverTime().plusMinutes(10);
        } else {
            end = LocalDateTime.now();
        }

        //拉时间序列
        List<Map<String, Object>> rows = metricLogMapper.selectMetricTrend(
                record.getInstanceId(), record.getMetricKey(), start, end);

        List<String> timestampList = new ArrayList<>();
        List<Double> valueList = new ArrayList<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                Object tm = row.get("tm");
                if (tm == null) {
                    continue;
                }
                timestampList.add(tm.toString());
                Object val = row.get("value");
                valueList.add(ParseUtils.stringToDouble(val.toString()));
            }
        }
        dto.setTimestampList(timestampList);
        dto.setValueList(valueList);
        return dto;
    }
}
