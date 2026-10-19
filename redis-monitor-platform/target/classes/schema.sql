-- ============================================================
-- Redis Monitor Platform 建表语句（Supabase / PostgreSQL）
-- ============================================================
/**
-- 执行器信息表
 */
CREATE TABLE IF NOT EXISTS executor_info (
    id                  character varying(32) PRIMARY KEY NOT NULL,
    executor_id         character varying(32),
    host_ip             character varying(20),
    executor_port       character varying(5),
    executor_status     character varying(3),
    last_heartbeat_time character varying(20),
    last_report_time    character varying(20),
    management_status   character varying(5),
    executor_token      character varying(50),
    created_time            timestamp  default CURRENT_TIMESTAMP NOT NULL,
    updated_time            timestamp  default CURRENT_TIMESTAMP NOT NULL
);
COMMENT ON TABLE executor_info IS '执行器信息表:记录执行器相关信息';
COMMENT ON COLUMN executor_info.id IS '表内部主键';
COMMENT ON COLUMN executor_info.executor_id IS '执行器唯一标识';
COMMENT ON COLUMN executor_info.host_ip IS '执行器所在服务器IP';
COMMENT ON COLUMN executor_info.executor_port IS '执行器HTTPS服务端口';
COMMENT ON COLUMN executor_info.executor_status IS '执行器状态：ONLINE、OFFLINE';
COMMENT ON COLUMN executor_info.last_heartbeat_time IS '最近一次心跳时间';
COMMENT ON COLUMN executor_info.last_report_time IS '最近一次Redis/Sentinel状态上报时间';
COMMENT ON COLUMN executor_info.management_status IS '纳管状态：ACTIVE、INACTIVE';
COMMENT ON COLUMN executor_info.executor_token IS '执行器校验token，加密存储';
COMMENT ON COLUMN executor_info.created_time IS '创建时间';
COMMENT ON COLUMN executor_info.updated_time IS '更新时间';

/**
-- Redis实例基础信息表
 */
CREATE TABLE IF NOT EXISTS redis_instance (
    id                  character varying(32) PRIMARY KEY NOT NULL,
    instance_name       character varying(100) NOT NULL,
    host_ip             character varying(20) NOT NULL,
    redis_port          character varying(5) NOT NULL,
    redis_password      character varying(512),
    role                character varying(4) NOT NULL,
    redis_version       character varying(32) NOT NULL,
    run_status          character varying(4) NOT NULL,
    master_host_ip      character varying(20),
    master_link_status  character varying(5),
    master_redis_port   character varying(5),
    last_heartbeat_time character varying(20) default'null'::character varying NOT NULL,
    management_status   character varying(5),
    created_time            timestamp  default CURRENT_TIMESTAMP NOT NULL,
    updated_time            timestamp  default CURRENT_TIMESTAMP NOT NULL
);
COMMENT ON TABLE redis_instance IS 'Redis实例表:保存已纳管Redis实例的连接、角色、运行状态、主从关系。';
COMMENT ON COLUMN redis_instance.id IS '实例id';
COMMENT ON COLUMN redis_instance.instance_name IS 'Redis实例名称';
COMMENT ON COLUMN redis_instance.host_ip IS '所在服务器IP';
COMMENT ON COLUMN redis_instance.redis_port IS 'Redis端口，与hostip_共同确定唯一实例';
COMMENT ON COLUMN redis_instance.redis_password IS 'Redis密码，加密保存';
COMMENT ON COLUMN redis_instance.role IS '节点角色：MASTER/REPLICA/UNKNOWN';
COMMENT ON COLUMN redis_instance.redis_version IS 'Redis版本';
COMMENT ON COLUMN redis_instance.run_status IS 'Redis当前运行状态：在线/离线/异常';
COMMENT ON COLUMN redis_instance.master_host_ip IS '当前主节点IP';
COMMENT ON COLUMN redis_instance.master_link_status IS '主从连接状态：UP/DOWN';
COMMENT ON COLUMN redis_instance.master_redis_port IS '当前主节点Redis端口';
COMMENT ON COLUMN redis_instance.last_heartbeat_time IS '最后心跳时间';
COMMENT ON COLUMN redis_instance.created_time IS '创建时间';
COMMENT ON COLUMN redis_instance.updated_time IS '更新时间';
COMMENT ON COLUMN redis_instance.management_status IS '纳管状态：ACTIVE、INACTIVE';

CREATE INDEX IF NOT EXISTS idx_redis_instance_status ON redis_instance(management_status, run_status,role);
alter table redis_instance add constraint redis_instance_uk unique (instance_name);


/**
-- 哨兵节点配置表
 */
CREATE TABLE IF NOT EXISTS sentinel_node_info (
    id                      character varying(32) NOT NULL,
    node_name               character varying(64) NOT NULL,
    node_ip                 character varying(20) NOT NULL,
    node_port               character varying(10) NOT NULL,
    sentinel_status         character varying(4) NOT NULL,
    last_heartbeat_time     timestamp  default CURRENT_TIMESTAMP NOT NULL,
    created_time            timestamp  default CURRENT_TIMESTAMP NOT NULL,
    updated_time            timestamp  default CURRENT_TIMESTAMP NOT NULL,
    management_status       character varying(5)
);

COMMENT ON TABLE sentinel_node_info IS '哨兵节点配置表:存储哨兵模式下节点的配置与在线状态';
COMMENT ON COLUMN sentinel_node_info.id IS '节点id（主键）';
COMMENT ON COLUMN sentinel_node_info.node_name IS '节点名称';
COMMENT ON COLUMN sentinel_node_info.node_ip IS '哨兵ip';
COMMENT ON COLUMN sentinel_node_info.node_port IS '哨兵端口';
COMMENT ON COLUMN sentinel_node_info.sentinel_status IS '在线状态：0=离线 1=在线';
COMMENT ON COLUMN sentinel_node_info.last_heartbeat_time IS '最后心跳时间';
COMMENT ON COLUMN sentinel_node_info.created_time IS '创建时间';
COMMENT ON COLUMN sentinel_node_info.updated_time IS '更新时间';
COMMENT ON COLUMN sentinel_node_info.management_status IS '纳管状态：ACTIVE、INACTIVE';

alter table sentinel_node_info add constraint sentinel_node_info_pk primary key (id);


/**
-- 告警规则表
 */
CREATE TABLE IF NOT EXISTS alarm_rule (
    rule_id             character varying(32) primary key NOT NULL,
    rule_name           character varying(128) NOT NULL,
    metric_key          character varying(64) NOT NULL,
    metric_name         character varying(64) NOT NULL,
    compare_type        character varying(8) default'0'::character varying NOT NULL,
    threshold           character varying(32) NOT NULL,
    collect_interval    character varying(16) default'300'::character varying NOT NULL,
    duration            character varying(16) default'2'::character varying NOT NULL,
    alarm_level         character varying(4) default'0'::character varying NOT NULL,
    silence_duration    character varying(16) default'1800'::character varying NOT NULL,
    scope_type          character varying(4) default'1'::character varying NOT NULL,
    scope_instance_id   character varying(1024) NOT NULL,
    enable_status       character varying(4) default'1'::character varying NOT NULL,
    del_flag            character varying(4) default'0'::character varying NOT NULL,
    created_by          character varying(32) NOT NULL,
    created_time        timestamp  default CURRENT_TIMESTAMP NOT NULL,
    updated_time        timestamp  default CURRENT_TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_alarm_rule_status ON alarm_rule(del_flag, enable_status);

COMMENT ON TABLE alarm_rule IS '告警规则配置表:存储所有监控指标的告警触发规则';
COMMENT ON COLUMN alarm_rule.rule_id IS '规则id';
COMMENT ON COLUMN alarm_rule.rule_name IS '规则名称';
COMMENT ON COLUMN alarm_rule.metric_key IS '指标key值';
COMMENT ON COLUMN alarm_rule.metric_name IS '指标名称';
COMMENT ON COLUMN alarm_rule.compare_type IS '比较方式';
COMMENT ON COLUMN alarm_rule.threshold IS '阈值';
COMMENT ON COLUMN alarm_rule.collect_interval IS '采集间隔/s';
COMMENT ON COLUMN alarm_rule.duration IS '持续触发时长/周期';
COMMENT ON COLUMN alarm_rule.alarm_level IS '告警级别';
COMMENT ON COLUMN alarm_rule.silence_duration IS '静默期/s';
COMMENT ON COLUMN alarm_rule.scope_type IS '生效范围：1=全部节点 2=指定节点';
COMMENT ON COLUMN alarm_rule.scope_instance_id IS '生效节点id列表';
COMMENT ON COLUMN alarm_rule.enable_status IS '启用状态：0禁1启';
COMMENT ON COLUMN alarm_rule.del_flag IS '逻辑删除：0=未删除 1=已删除';
COMMENT ON COLUMN alarm_rule.created_by IS '创建人id';
COMMENT ON COLUMN alarm_rule.created_time IS '创建时间';
COMMENT ON COLUMN alarm_rule.updated_time IS '更新时间';


/**
-- 告警记录表
 */
CREATE TABLE IF NOT EXISTS alarm_record (
    id                  character varying(32) PRIMARY KEY NOT NULL,
    alarm_no            character varying(32) NOT NULL,
    rule_id             character varying(32) NOT NULL,
    instance_id         character varying(32) NOT NULL,
    metric_key          character varying(64) NOT NULL,
    metric_name         character varying(64) NOT NULL,
    alarm_level         character varying(4) NOT NULL,
    alarm_content       character varying(256) NOT NULL,
    trigger_value       character varying(32) NOT NULL,
    threshold           character varying(32) NOT NULL,
    trigger_time        timestamp NOT NULL,
    recover_time        timestamp NOT NULL,
    recover_status      character varying(4) default'0'::character varying NOT NULL,
    created_time        timestamp  default CURRENT_TIMESTAMP NOT NULL,
    updated_time        timestamp  default CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE alarm_record IS '告警记录表:存储所有触发的告警事件及恢复状态';
COMMENT ON COLUMN alarm_record.id IS '主键告警记录id';
COMMENT ON COLUMN alarm_record.alarm_no IS '告警编号';
COMMENT ON COLUMN alarm_record.rule_id IS '关联规则id';
COMMENT ON COLUMN alarm_record.instance_id IS '触发告警实例id';
COMMENT ON COLUMN alarm_record.metric_key IS '监控指标key值';
COMMENT ON COLUMN alarm_record.metric_name IS '监控指标名称';
COMMENT ON COLUMN alarm_record.alarm_level IS '告警级别';
COMMENT ON COLUMN alarm_record.alarm_content IS '告警内容';
COMMENT ON COLUMN alarm_record.trigger_value IS '触发告警指标值';
COMMENT ON COLUMN alarm_record.threshold IS '阈值';
COMMENT ON COLUMN alarm_record.trigger_time IS '触发时间';
COMMENT ON COLUMN alarm_record.recover_time IS '修复时间';
COMMENT ON COLUMN alarm_record.recover_status IS '修复状态：0=未恢复 1=已恢复';
COMMENT ON COLUMN alarm_record.created_time IS '创建时间';
COMMENT ON COLUMN alarm_record.updated_time IS '更新时间';


CREATE INDEX IF NOT EXISTS idx_alarm_record_time ON alarm_record(trigger_time DESC);
CREATE INDEX IF NOT EXISTS idx_alarm_record_status ON alarm_record(recover_status);


/**
-- 主从切换事件表
 */
CREATE TABLE IF NOT EXISTS redis_switch_event (
    id                  character varying(32) PRIMARY KEY NOT NULL,
    old_master_id       character varying(32) NOT NULL,
    new_master_id       character varying(32) NOT NULL,
    switch_time         timestamp  default CURRENT_TIMESTAMP NOT NULL,
    switch_type         character varying(256) NOT NULL,
    switch_result       character varying(4) default'1'::character varying NOT NULL,
    created_time        timestamp  default CURRENT_TIMESTAMP NOT NULL,
    updated_time        timestamp  default CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE redis_switch_event IS '主从切换事件表:存储每次主从角色切换的完整时间记录，用于切换追溯与统计';
COMMENT ON COLUMN redis_switch_event.id IS '事件id';
COMMENT ON COLUMN redis_switch_event.old_master_id IS '原主节点id';
COMMENT ON COLUMN redis_switch_event.new_master_id IS '新主节点id';
COMMENT ON COLUMN redis_switch_event.switch_time IS '切换时间';
COMMENT ON COLUMN redis_switch_event.switch_type IS '切换类型：sentinel自动故障转移/手动切换/网络故障';
COMMENT ON COLUMN redis_switch_event.switch_result IS '切换结果：0=失败 1=成功';
COMMENT ON COLUMN redis_switch_event.created_time IS '创建时间';
COMMENT ON COLUMN redis_switch_event.updated_time IS '更新时间';


/**
-- 监控指标数据采集表
 */
CREATE TABLE IF NOT EXISTS rms_metric_log (
    id                  character varying(32) PRIMARY KEY NOT NULL,
    agent_id            character varying(32) NOT NULL,
    tm                  TIMESTAMP NOT NULL,
    cpu_per             character varying(20) NOT NULL,
    mem_sever_per       character varying(20) NOT NULL,
    mem_us              character varying(30) NOT NULL,
    latency             character varying(20) NOT NULL,
    io_iik              character varying(20) NOT NULL,
    io_iok              character varying(20) NOT NULL,
    ops                 character varying(30) NOT NULL,
    repl_delay          character varying(20),
    run_id              character varying(100) NOT NULL,
    master_host         character varying(20) NOT NULL,
    master_port         character varying(5) NOT NULL,
    redis_version       character varying(32) NOT NULL,
    collect_type        character varying(4) NOT NULL,
    created_time        timestamp  default CURRENT_TIMESTAMP NOT NULL,
    updated_time        timestamp  default CURRENT_TIMESTAMP NOT NULL
);

COMMENT ON TABLE rms_metric_log IS '监控指标数据采集表:定时采集任务数据记录表';
COMMENT ON COLUMN rms_metric_log.id IS '主键';
COMMENT ON COLUMN rms_metric_log.agent_id IS '执行器ID';
COMMENT ON COLUMN rms_metric_log.tm IS '采集时间';
COMMENT ON COLUMN rms_metric_log.cpu_per IS 'cpu占用率（%）';
COMMENT ON COLUMN rms_metric_log.mem_sever_per IS '服务器内存占用率（%）';
COMMENT ON COLUMN rms_metric_log.mem_us IS 'redis内存使用量/字节';
COMMENT ON COLUMN rms_metric_log.latency IS 'redis延迟/µs';
COMMENT ON COLUMN rms_metric_log.io_iik IS '网络输入带宽（kbps）';
COMMENT ON COLUMN rms_metric_log.io_iok IS '网络输出带宽（kbps）';
COMMENT ON COLUMN rms_metric_log.ops IS '瞬时qps（次/秒）';
COMMENT ON COLUMN rms_metric_log.repl_delay IS '主从复制延迟/秒';
COMMENT ON COLUMN rms_metric_log.run_id IS '节点运行id';
COMMENT ON COLUMN rms_metric_log.master_host IS '主节点主机';
COMMENT ON COLUMN rms_metric_log.master_port IS '主节点端口';
COMMENT ON COLUMN rms_metric_log.redis_version IS 'redis版本号';
COMMENT ON COLUMN rms_metric_log.collect_type IS '采集类型：0=自动/1=手动';
COMMENT ON COLUMN rms_metric_log.created_time IS '创建时间';
COMMENT ON COLUMN rms_metric_log.updated_time IS '更新时间';

CREATE INDEX IF NOT EXISTS idx_metric_agent_tm ON rms_metric_log(agent_id, tm);


/**
-- 节点状态采集表
 */
CREATE TABLE IF NOT EXISTS rms_node_status_log (
    id                  character varying(32) PRIMARY KEY NOT NULL,
    agent_id            character varying(32) NOT NULL,
    instance_id         character varying(32) NOT NULL,
    run_id              character varying(100) NOT NULL,
    tm                  character varying(20) NOT NULL,
    collect_status      character varying(20) NOT NULL,
    error_msg           character varying(500) NOT NULL,
    role                character varying(30) NOT NULL,
    ping_latency        character varying(20) NOT NULL,
    created_time        timestamp  default CURRENT_TIMESTAMP NOT NULL,
    updated_time        timestamp  default CURRENT_TIMESTAMP NOT NULL
    );

COMMENT ON TABLE rms_node_status_log IS '节点状态采集表:定时采集节点状态记录表';
COMMENT ON COLUMN rms_node_status_log.id IS '主键';
COMMENT ON COLUMN rms_node_status_log.agent_id IS '执行器ID';
COMMENT ON COLUMN rms_node_status_log.instance_id IS 'Redis实例ID';
COMMENT ON COLUMN rms_node_status_log.run_id IS '节点运行id';
COMMENT ON COLUMN rms_node_status_log.tm IS '采集时间';
COMMENT ON COLUMN rms_node_status_log.collect_status IS '采集时节点状态：0=离线 1=在线';
COMMENT ON COLUMN rms_node_status_log.error_msg IS '失败原因：连接超时/认证失败、拒绝连接等';
COMMENT ON COLUMN rms_node_status_log.role IS '当前角色';
COMMENT ON COLUMN rms_node_status_log.ping_latency IS '连通性延迟/ms';
COMMENT ON COLUMN rms_node_status_log.created_time IS '创建时间';
COMMENT ON COLUMN rms_node_status_log.updated_time IS '更新时间';







/**


-- 操作审计日志表
CREATE TABLE IF NOT EXISTS operation_audit_log (
                                                   log_id             VARCHAR(50) PRIMARY KEY,
    operator_id        VARCHAR(50),
    operator_name      VARCHAR(50),
    business_id        VARCHAR(50),
    business_type      VARCHAR(50),
    operation_type     VARCHAR(50),
    detail             VARCHAR(1000),
    operation_time     TIMESTAMP
    );

-- 报告导出任务表
CREATE TABLE IF NOT EXISTS report_export_task (
                                                  task_id            VARCHAR(50) PRIMARY KEY,
    instance_id        VARCHAR(50),
    report_type        VARCHAR(20),
    task_status        VARCHAR(20),
    download_url       VARCHAR(500),
    fail_reason        VARCHAR(500),
    create_time        TIMESTAMP,
    finish_time        TIMESTAMP
    );

 */
