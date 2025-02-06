package org.yx.hoststack.center.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 镜像信息
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_image_download_inf")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageDownloadInf implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

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