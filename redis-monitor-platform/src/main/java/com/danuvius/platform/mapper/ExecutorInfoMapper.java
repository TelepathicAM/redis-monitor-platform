package com.danuvius.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.danuvius.platform.model.entity.ExecutorInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExecutorInfoMapper extends BaseMapper<ExecutorInfo> {
    ExecutorInfo selectByHostIpAndPort(@Param("hostIp") String hostIp,
                                       @Param("executorPort") String executorPort);

    ExecutorInfo selectByToken(@Param("executorToken") String executorToken);
    ExecutorInfo selectByInstanceId(@Param("instanceId") String instanceId);

    /*查询纳管且在线的执行器*/
    List<ExecutorInfo> selectActiveOnlineExecutors(@Param("managementStatus") String managementStatus,
                                                   @Param("executorStatus") String executorStatus);

}
