package org.yx.hoststack.center.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HOST/CONTAINER-AGENT会话信息
 *
 * @author lyc
 * @since 2024-12-19 19:27:24
 */
@Data
@TableName("t_agent_session")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgentSession implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  HOSTAGENTID/CONTAINERAGENTID
     */
    private String agentId;

    /**
     *   
     */
    private String zone;

    /**
     *   
     */
    private String region;

    /**
     *   
     */
    private String idc;

    /**
     *  HOST/CONTAINER
     */
    private String agentType;

    /**
     *  资源池类型：IDC/EDGE/HE
     */
    private String resourcePool;

    /**
     *  容器个数，当AGENT_TYPE等于HOST时，该字段有效
     */
    private Integer containerCount;

    /**
     *  CPU利用率，单位%
     */
    private Integer cpuUsage;

    /**
     *  内存利用率，单位%
     */
    private Integer memoryUsage;

    /**
     *  GPU利用率，单位%
     */
    private Integer gpuUsage;

    /**
     *  GPU温度，单位℃
     */
    private Integer gpuTemperature;

    /**
     *  GPU风扇转速，单位%
     */
    private Integer gpuFanSpeed;

    /**
     *  AGENTIP信息
     */
    private String agentIp;

    /**
     *  会话创建时间
     */
    private Date createTime;

}