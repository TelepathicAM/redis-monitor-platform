package com.danuvius.platform.model.dto.monitor;

import lombok.Data;

import java.io.Serializable;

@Data
public class TopologyNodeDTO implements Serializable {
    /** 实例节点id */
    private String instanceId;
    /** 实例节点名 */
    private String instanceName;
    /** 节点ip */
    private String ip;
    /** 节点端口 */
    private String port;
    /** 节点角色 */
    private String role;
    /** 所属主节点 id:port */
    private String masterName;
    /** 复制延迟 */
    private String copyDelay;
    /*复制状态： 0=正常/1=延迟*/
    /** 复制状态：正常 / 延迟 */
    private String copyStatus;
    /*整体拓扑状态： 0=健康/1=警告/2=异常*/

}