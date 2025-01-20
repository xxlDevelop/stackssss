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
 * HOST/CONTAINER-AGENT CPU信息
 *
 * @author lyc
 * @since 2024-12-19 19:27:24
 */
@Data
@TableName("t_agent_cpu")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgentCpu implements Serializable{

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
     *  CPU数量
     */
    private Integer cpuNum;

    /**
     *  CPU类型
     */
    private String cpuType;

    /**
     *  CPU厂商
     */
    private String cpuManufacturer;

    /**
     *  CPU架构
     */
    private String cpuArchitecture;

    /**
     *  CPU核心
     */
    private Integer cpuCores;

    /**
     *  CPU线程
     */
    private Integer cpuThreads;

    /**
     *  CPU速率
     */
    private String cpuBaseSpeed;

}