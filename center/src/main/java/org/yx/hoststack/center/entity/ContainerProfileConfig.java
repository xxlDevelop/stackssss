package org.yx.hoststack.center.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 容器网络配置
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_container_profile_config")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContainerProfileConfig implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *   
     */
    private String containerId;

    /**
     *  容器所属宿主机资源ID
     */
    private String hostId;

    /**
     *  镜像ID
     */
    private String imageId;

    /**
     *  镜像版本
     */
    private String imageVer;

    /**
     *  创建容器时使用的配置
     */
    private String profile;

}