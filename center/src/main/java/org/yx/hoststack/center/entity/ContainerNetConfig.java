package org.yx.hoststack.center.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 容器IP配置信息表
 * </p>
 *
 * @author Lee666
 * @since 2024-12-25
 */
@TableName("t_container_net_config")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContainerNetConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 容器ID
     */
    private String cid;

    /**
     *  机房标识
     */
    private String idc;

    /**
     * 容器IP
     */
    private String ip;

    /**
     * 容器网关
     */
    private String gateway;

    /**
     * 容器子网掩码
     */
    private String subNetMask;

    /**
     * 容器dns1
     */
    private String dns1;

    /**
     * 容器dns2
     */
    private String dns2;

    /**
     * 容器mac地址
     */
    private String mac;

    /**
     * 网卡名称
     */
    private String interfaceName;

    /**
     * 网卡工作模式：bridge、nat
     */
    private String netMode;

    /**
     * 最后修改时间
     */
    private Date lastUpdateAt;

}
