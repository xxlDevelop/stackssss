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
@TableName("t_image_info")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

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
    private String contianerType;

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

    /**
     * If the tenant ID to which the image belongs is uploaded by the administrator, TID is 10000
     */
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

    /**
     * 最后修改时间戳
     */
    private Timestamp lastUpdateAt;

    /**
     * 最后修改账户ID
     */
    private String lastUpldateAccount;

}