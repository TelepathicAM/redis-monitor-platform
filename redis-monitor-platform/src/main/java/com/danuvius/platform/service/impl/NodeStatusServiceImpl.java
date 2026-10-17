package com.danuvius.platform.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.danuvius.platform.mapper.NodeStatusMapper;
import com.danuvius.platform.model.entity.NodeStatus;
import com.danuvius.platform.service.NodeStatusService;
import org.springframework.stereotype.Service;

@Service
public class NodeStatusServiceImpl extends ServiceImpl<NodeStatusMapper, NodeStatus>
        implements NodeStatusService {
}
