package org.yx.hoststack.center.entity;

import java.io.Serial;
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
 * Agent信息表,存储hostAgent和containerAgent信息
 * @author Lee666
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_host")
public class Host implements Serializable {
    @Serial
    private static final long serialVersionUID = -3393744176451974020L;
    /**
     * hostAgent唯一标识
     */
    @TableId(value = "host_id", type = IdType.INPUT)
    private String hostId;

    /**
     * agent版本号
     */
    private String agentVersion;

    /**
     * 资源注册模式：host，container，benchmark
     */
    private String agentType;

    /**
     * 节点启动时间
     */
    private Date startTime;

    /**
     * 设备编码
     */
    private String devSn;

    /**
     * 操作系统类型
     */
    private String osType;

    /**
     * 操作系统版本
     */
    private String osVersion;

    /**
     * 操作系统内存
     */
    private String osMem;

    /**
     * 资源池类型，idc、edge、he
     */
    private String resourcePool;

    /**
     * 当前操作系统虚拟化信息：bm （裸金属），docker ，vm
     */
    private String runtimeEnv;

    /**
     * 磁盘信息
     */
    private String diskInfo;

    /**
     * 网卡信息
     */
    private String networkCardInfo;

    /**
     * host所属大区标识
     */
    private String zone;

    /**
     * host所属分区标识
     */
    private String region;

    /**
     * host所属relay标识
     */
    private String relay;

    /**
     * host所属IDC标识
     */
    private String idc;

    /**
     * hostIP地址
     */
    private String hostIp;

    /**
     * gpu数量
     */
    private Integer gpuNum;

    /**
     * cpu数量
     */
    private Integer cpuNum;

    /**
     * 详细的硬件唯一标识信息，采用key:value格式，例如“cpuId:123,biosId:456”
     */
    private String detailedId;

    /**
     * 网络代理 0-否，1-是
     */
    private Integer proxy;

    /**
     * BareMetal提供者ID

     */
    private String baremetalProvider;

    /**
     * BareMetal提供者所属租户ID
     */
    private Long providerTenantId;

    /**
     * 最后心跳时间
     */
    private Date lastHbAt;

    /**
     * 访问中心的Ak
     */
    private String ak;

    /**
     * 访问中心的Sk, AES加密
     */
    private String sk;

    /**
     * 创建时间戳
     */
    private Date createAt;

}