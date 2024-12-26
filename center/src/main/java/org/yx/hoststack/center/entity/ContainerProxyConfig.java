package org.yx.hoststack.center.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 容器代理信息配置
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_container_proxy_config")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContainerProxyConfig implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  容器ID
     */
    private String containerId;

    /**
     *  代理服务端URL
     */
    private String domain;

    /**
     *  代理IP
     */
    private String proxyIp;

    /**
     *  访问代理服务的用户名
     */
    private String proxyUser;

    /**
     *  访问代理服务的密码
     */
    private String proxyPsw;

    /**
     *  需要执行代理网络的应用名单,多个用逗号分隔
     */
    private String whilteApps;

    /**
     *  是否要自动开启代理网路, TRUE:需要开启, FALSE:不开启
     */
    private String enableProxyNetwork;

    /**
     *  创建时间
     */
    private Date createAt;

}