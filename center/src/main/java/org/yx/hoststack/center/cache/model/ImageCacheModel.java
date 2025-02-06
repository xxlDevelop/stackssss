package org.yx.hoststack.center.cache.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageCacheModel {
    /**
     * 镜像ID
     */
    private String imageId;

    /**
     * 镜像名称
     */
    private String imageName;

    /**
     * 镜像版本号
     */
    private String imageVer;

    /**
     * 镜像类型
     */
    private String imageType;

    /**
     * The business types applicable to the image, render/ai
     */
    private String bizType;

    /**
     * 镜像适用的资源池类型 EDGE/IDC
     */
    private String resourcePool;

    /**
     * 镜像操作系统类型, WINDOWS, LINUX, ANDROID
     */
    private String osType;

    /**
     * 镜像虚拟化类型: DOCKER/KVM
     */
    private String containerType;

    /**
     * 镜像存储路径
     */
    private String storagePath;

    /**
     * 镜像下载地址
     */
    private String downloadUrl;

    /**
     * 镜像文件的MD5值
     */
    private String md5;

    /**
     * LABEL标签
     */
    private String label;


    private Long tenantId;

    /**
     * 是否是官方镜像
     */
    private Boolean isOfficial;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 创建时间戳
     */
    private Timestamp createAt;

    private Timestamp lastUpdateAt;
}
