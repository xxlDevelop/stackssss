package org.yx.hoststack.center.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * host/container-agent会话信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_agent_session")
public class AgentSession implements Serializable {
    /**
    * hostAgentId/containerAgentId
    */
    private String agentId;

    private String zone;

    private String region;

    private String idc;

    /**
    * host/container
    */
    private String agentType;

    /**
    * 资源池类型：idc/edge/he
    */
    private String resourcePool;

    /**
    * 容器个数，当agent_type等于host时，该字段有效
    */
    private Integer containerCount;

    /**
    * CPU利用率，单位%
    */
    private Integer cpuUsage;

    /**
    * 内存利用率，单位%
    */
    private Integer memoryUsage;

    /**
    * GPU利用率，单位%
    */
    private Integer gpuUsage;

    /**
    * GPU温度，单位℃
    */
    private Integer gpuTemperature;

    /**
    * GPU风扇转速，单位%
    */
    private Integer gpuFanSpeed;

    /**
    * agentIp信息
    */
    private String agentIp;

    /**
    * 会话创建时间
    */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}