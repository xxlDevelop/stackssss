package org.yx.hoststack.center.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Container information
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_container")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Container implements Serializable {

    @Serial
    private static final long serialVersionUID = 4761804766431502509L;
    /**
     * Custom primary key HostId + # + auto-increment sequence number
     */
    @TableId(value = "container_id", type = IdType.INPUT)
    private String containerId;

    /**
     * Host resource ID to which the container belongs, not null when created via the hoststack platform
     */
    private String hostId;

    /**
     * Auto-increment sequence number; combined with HostId to define a unique primary key
     */
    private Integer sequenceNumber;

    /**
     * Image ID, not null when created via the hoststack platform
     */
    private String imageId;

    /**
     * Image version, not null when created via the hoststack platform
     */
    private String imageVer;

    /**
     * Host version
     */
    private String agentVersion;

    /**
     * Resource registration mode: host, container, benchmark
     */
    private String agentType;

    /**
     * Node start time
     */
    private Date startTime;

    /**
     * Device serial number
     */
    private String devSn;

    /**
     * Operating system type
     */
    private String osType;

    /**
     * Operating system version
     */
    private String osVersion;

    /**
     * Operating system memory, in MB
     */
    private String osMem;

    /**
     * Resource pool type: idc, edge, he
     */
    private String resourcePool;

    /**
     * Current operating system virtualization information: bm (bare metal), docker, vm
     */
    private String runtimeEnv;

    /**
     * Disk information
     */
    private String diskInfo;

    /**
     * Network card information
     */
    private String networkCardInfo;

    /**
     * Zone identifier to which the container belongs
     */
    private String zone;

    /**
     * Region identifier to which the container belongs
     */
    private String region;

    /**
     * Relay identifier to which the container belongs
     */
    private String relay;

    /**
     * IDC identifier to which the container belongs
     */
    private String idc;

    /**
     * Container IP address
     */
    private String containerIp;

    /**
     * Number of GPUs
     */
    private Integer gpuNum;

    /**
     * Number of CPUs
     */
    private Integer cpuNum;

    /**
     * Detailed unique hardware identifier information in key:value format, e.g., "cpuId:123,biosId:456"
     */
    private String detailedId;

    /**
     * Network proxy: 0-No, 1-Yes
     */
    private Integer proxy;

    /**
     * Container provider ID
     */
    private String containerProvider;

    /**
     * Container provider tenant ID
     */
    private String providerTenantId;

    /**
     * Last heartbeat time
     */
    private Date lastHbAt;

    /**
     * Access center Ak
     */
    private String ak;

    /**
     * Access center Sk, AES encrypted
     */
    private String sk;

    /**
     * Creation timestamp
     */
    private Timestamp createAt;

}