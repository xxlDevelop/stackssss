package org.yx.hoststack.center.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent信息表,存储hostAgent和containerAgent信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_host")
public class Host implements Serializable {
    /**
     * hostAgent唯一标识
     */
    private String hostId;

    /**
     * host版本号
     */
    private String hostVersion;

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
     * host所属idc机房
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
     * BareMetal提供者ID

     */
    private String baremetalProvider;

    /**
     * BareMetal所属租户ID
     */
    private Long tenantId;

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
     * Checker公钥
     */
    private String checkerPublicKey;

    /**
     * 创建时间戳
     */
    private Date createAt;

    private static final long serialVersionUID = 1L;
}