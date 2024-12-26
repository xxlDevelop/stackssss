package org.yx.hoststack.center.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 创建容器配置
 * </p>
 *
 * @author Lee666
 * @since 2024-12-25
 */
@TableName("t_container_create_profile")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContainerCreateProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = -233366858064417334L;
    @TableId
    private String containerId;

    /**
     * 容器所属宿主机资源ID
     */
    private String hostId;

    /**
     * 镜像ID
     */
    private String imageId;

    /**
     * 镜像版本
     */
    private String imageVer;

    /**
     * 用户自定义数据
     */
    private String profileUserData;

    /**
     * 创建容器时使用的容器模板ID
     */
    private Long profileTemplateId;

    /**
     * 操作系统类型：windows 、linux
     */
    private String osType;

    /**
     * 内存大小
     */
    private String memory;

    /**
     * cpu核心数
     */
    private Integer vCpus;

    /**
     * 显卡授权token
     */
    private String license;

}
