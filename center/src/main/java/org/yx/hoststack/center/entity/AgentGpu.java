package org.yx.hoststack.center.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HOST/CONTAINER-AGENT GPU信息
 *
 * @author lyc
 * @since 2024-12-19 19:27:24
 */
@Data
@TableName("t_agent_gpu")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgentGpu implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *  HOSTID/CONTAINERID
     */
    private String agentId;

    /**
     *  GPU类型
     */
    private String gpuType;

    /**
     *  制作厂商
     */
    private String gpuManufacturer;

    /**
     *  显存
     */
    private String gpuMem;

    /**
     *  BUS类型
     */
    private String gpuBusType;

    /**
     *  GPU设备ID
     */
    private String gpuDeviceId;

    /**
     *  LINUX宿主机显卡的PCIE 设备地址，用于显卡直通时指定虚拟机使用的显卡
     */
    private String gpuBusId;

}