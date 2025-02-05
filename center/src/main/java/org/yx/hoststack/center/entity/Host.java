package org.yx.hoststack.center.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AGENT信息表,存储HOSTAGENT和CONTAINERAGENT信息
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_host")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Host implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  HOSTAGENT唯一标识
     */
    private String hostId;

    /**
     *  HOST版本号
     */
    private String hostVersion;

    /**
     *  节点启动时间
     */
    private Date startTime;

    /**
     *  设备编码
     */
    private String devSn;

    /**
     *  操作系统类型
     */
    private String osType;

    /**
     *  操作系统版本
     */
    private String osVersion;

    /**
     *  操作系统内存
     */
    private String osMem;

    /**
     *  磁盘信息
     */
    private String diskInfo;

    /**
     *  网卡信息
     */
    private String networkCardInfo;

    /**
     *  HOST所属大区标识
     */
    private String zone;

    /**
     *  HOST所属分区标识
     */
    private String region;

    /**
     *  HOST所属IDC机房
     */
    private String idc;

    /**
     *  HOSTIP地址
     */
    private String hostIp;

    /**
     *  GPU数量
     */
    private Integer gpuNum;

    /**
     *  CPU数量
     */
    private Integer cpuNum;

    /**
     *  BAREMETAL提供者ID

     */
    private String baremetalProvider;

    /**
     *  BAREMETAL所属租户ID
     */
    private Long tenantId;

    /**
     *  最后心跳时间
     */
    private Date lastHbAt;

    /**
     *  访问中心的AK
     */
    private String ak;

    /**
     *  访问中心的SK, AES加密
     */
    private String sk;

    /**
     *  CHECKER公钥
     */
    private String checkerPublicKey;

    /**
     *  创建时间戳
     */
    private Date createAt;

}