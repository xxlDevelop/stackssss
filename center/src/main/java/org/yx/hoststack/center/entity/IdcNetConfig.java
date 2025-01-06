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
 * 机房网络配置
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_idc_net_config")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IdcNetConfig implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     *  IDCID
     */
    private Long idcId;

    /**
     *  机房标识
     */
    private String idc;

    /**
     *  内网IP
     */
    private String localIp;

    /**
     *  内网端口
     */
    private Integer localPort;

    /**
     *  内网网关
     */
    private String gateway;

    /**
     *  子网掩码
     */
    private String mask;

    /**
     *  DNS1
     */
    private String dns1;

    /**
     *  DNS2
     */
    private String dns2;

    /**
     *  外网IP映射
     */
    private String mappingIp;

    /**
     *  外网端口映射
     */
    private Integer mappingPort;

    /**
     *  协议类型, HTTPS/WSS/TCP/UDP
     */
    private String netProtocol;

    /**
     *  上行网络带宽限制, 单位KB
     */
    private Long bandwidthInLimit;

    /**
     *  下行网络带宽限制, 单位KB
     */
    private Long bandwidthOutLimit;

    /**
     *  外网网络ISP类型
     */
    private String netIspType;

    /**
     *  IP类型, IPV4, IPV6
     */
    private String ipType;

    /**
     *  映射名称
     */
    private String mappingName;

}