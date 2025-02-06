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
public class ImageDownloadInfCacheModel {
    /**
     * 镜像ID
     */
    private String imageId;

    /**
     * 镜像版本号
     */
    private String imageVer;

    /**
     * 镜像所在区域
     */
    private String region;

    /**
     * 镜像所在机房
     */
    private String idc;

    /**
     * 镜像在机房的内网下载地址
     */
    private String localDownloadUrl;

    /**
     * 镜像公网下载地址
     */
    private String netDownloadUrl;

    private String md5;

    private long tenantId;

    private Timestamp createAt;
}
