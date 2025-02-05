package org.yx.hoststack.center.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 容器信息
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_container")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Container implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  容器ID
     */
    private String containerId;

    /**
     *  容器IP
     */
    private String ip;

    /**
     *  容器名称
     */
    private String name;

    /**
     *  标签
     */
    private String label;

    /**
     *  容器运行状态, NORMAL/SHUTDOWN/OFFLINE
     */
    private String status;

    /**
     *  系统镜像ID
     */
    private String imageId;

    /**
     *  系统镜像版本号
     */
    private String imageVer;

    /**
     *  容器所属宿主机资源ID
     */
    private String hostId;

    /**
     *  容器运行的业务类型, RENDER/AI
     */
    private String bizType;

    /**
     *  容器所属大区标识
     */
    private String zone;

    /**
     *  容器所属国家分区
     */
    private String region;

    /**
     *  容器所属机房标识
     */
    private String idc;

    /**
     *  容器的资源池类型 EDGE/IDC
     */
    private String resourcePool;

    /**
     *  容器操作系统类型, WINDOWS, LINUX, ANDROID
     */
    private String osType;

    /**
     *  容器化类型: DOCKER/KVM
     */
    private String contianerType;

    /**
     *  容器创建时间戳
     */
    private Date createAt;

    /**
     *  容器最后心跳时间
     */
    private Date lastHbAt;

}