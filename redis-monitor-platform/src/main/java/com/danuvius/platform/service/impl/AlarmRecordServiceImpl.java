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
import com.danuvius.platform.model.entity.AuditLog;
import com.danuvius.platform.model.enums.StatusEnum;
import com.danuvius.platform.service.AlarmNoticeService;
import com.danuvius.platform.service.AlarmRecordService;
import com.danuvius.platform.service.AuditLogService;
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
    private final AuditLogService auditLogService;

    /*告警详情需关联实例信息与通知记录 */
    private final RedisInstanceMapper redisInstanceMapper;
    private final AlarmNoticeService alarmNoticeService;
    private  final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public AlarmRecordServiceImpl(AlarmRuleMapper ruleMapper, AlarmRecordMapper recordMapper,
                                  MetricLogMapper metricLogMapper, RedisInstanceMapper redisInstanceMapper,
                                  AuditLogService auditLogService, AlarmNoticeService alarmNoticeService
                                  ) {
        this.recordMapper = recordMapper;
        this.ruleMapper = ruleMapper;
        this.metricLogMapper = metricLogMapper;
        this.redisInstanceMapper = redisInstanceMapper;
        this.auditLogService = auditLogService;
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
        if (startTime == null || startTime.trim().isEmpty()) startTime = start.toLocalDate().atStartOfDay().format(FORMATTER);
        if (endTime == null || endTime.trim().isEmpty()) endTime = end.format(FORMATTER);
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
        String alarmLevel=req.getAlarmLevel();
        String metricKey=req.getMetricKey();
        String recoverStatus=req.getRecoverStatus();
        String keyword=req.getKeyword();
        String startTime=req.getStartTime();
        String endTime=req.getEndTime();

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(3);
        if (startTime == null || startTime.trim().isEmpty()) startTime = start.toLocalDate().atStartOfDay().format(FORMATTER);
        if (endTime == null || endTime.trim().isEmpty()) endTime = end.format(FORMATTER);

        //查
        List<RecordItemResponse> list = recordMapper.selectRecordPage(
                instanceId, alarmLevel, metricKey, recoverStatus, keyword, startTime, endTime,offset,pageSize);
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
            dto.setRuleInfo(ruleMapper.selectByRuleId(record.getRuleId()));
        }
        /* 关联节点信息表查询实例名、ip、端口、角色 */
        if (record.getInstanceId() != null) {
            dto.setInstanceInfo(redisInstanceMapper.selectByInstanceId(record.getInstanceId()));
        }
        /* 查询通知发送记录表，返回邮件通知发送时间、接收人、发送状态 */
        dto.setNoticeLogs(alarmNoticeService.listByAlarmRecordId(record.getId()));
        /*人工确认恢复后，从操作审计日志填充处理人信息*/
        fillOperatorInfo(dto, alarmRecordId);
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
        if ("1".equals(record.getRecoverStatus()) && record.getRecoverTime() != null) {
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
                valueList.add(val == null ? null : ParseUtils.stringToDouble(val.toString()));
            }
        }
        dto.setTimestampList(timestampList);
        dto.setValueList(valueList);
        return dto;
    }

    /**
     * 从操作审计日志填充告警处理人信息（人工确认恢复记录）
     * @param dto
     * @param alarmRecordId
     */
    private void fillOperatorInfo(RecordDetailResponse dto, String alarmRecordId) {
        try {
            AuditLog latest = auditLogService.lambdaQuery()
                    .eq(AuditLog::getTargetId, alarmRecordId)
                    .eq(AuditLog::getOperationType, "恢复")
                    .orderByDesc(AuditLog::getOperationTime)
                    .last("LIMIT 1")
                    .one();
            if (latest != null) {
                dto.setOperatorId(latest.getOperatorId());
                dto.setOperatorName(latest.getOperatorName());
                dto.setOperationTime(latest.getOperationTime() == null
                        ? null : latest.getOperationTime().format(FORMATTER));
                dto.setOperationMsg(latest.getOperationContent());
            }
        } catch (Exception e) {
            // 处理人信息获取失败不影响详情主流程
            log.warn("[告警详情] 获取处理人信息失败, alarmRecordId={}", alarmRecordId, e);
        }
    }


    /**
     * 告警人工确认恢复：
     * 1) 校验告警记录存在；
     * 2) 已恢复则幂等返回 true，不重复写审计日志；
     * 3) 更新 recover_status=1、recover_time=当前时间、updated_time=当前时间；
     * 4) 记入操作审计日志（module=告警记录，operationType=恢复）。
     */
    @Override
    public boolean recoverAlarm(String alarmRecordId) {
        AlarmRecord record = recordMapper.selectById(alarmRecordId);
        if (record == null) {
            throw new BusinessException(BusinessCode.NOT_FOUND_ERROR.getCode(), "告警记录不存在");
        }
        //已恢复幂等返回，不重复写审计日志
        if (StatusEnum.RECOVER.getCode().equals(record.getRecoverStatus())) {
            return true;
        }
        LocalDateTime now = LocalDateTime.now();
        record.setRecoverStatus(StatusEnum.RECOVER.getCode());
        record.setRecoverTime(now);
        record.setUpdatedTime(now);
        int rows = recordMapper.updateById(record);
        if (rows > 0) {
            //人工确认恢复记入操作审计日志
            auditLogService.record("告警记录", "恢复", alarmRecordId,
                    "人工确认恢复告警：" + record.getAlarmNo());
        }
        return rows > 0;
    }
}
