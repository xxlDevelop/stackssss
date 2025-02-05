package org.yx.hoststack.center.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class ImageInfo implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  镜像ID
     */
    private String imageId;

    /**
     *  镜像名称
     */
    private String imageName;

    /**
     *  镜像版本号
     */
    private String imageVer;

    /**
     *  镜像适用的业务类型, RENDER/AI
     */
    private String bizType;

    /**
     *  镜像适用的资源池类型 EDGE/IDC
     */
    private String resourcePool;

    /**
     *  镜像操作系统类型, WINDOWS, LINUX, ANDROID
     */
    private String osType;

    /**
     *  镜像虚拟化类型: DOCKER/KVM
     */
    private String contianerType;

    /**
     *  镜像存储路径
     */
    private String storagePath;

    /**
     *  镜像下载地址
     */
    private String downloadUrl;

    /**
     *  镜像文件的MD5值
     */
    private String md5;

    /**
     *  LABEL标签
     */
    private String label;

    /**
     *  镜像所属租户ID, 如果是管理员上传, TID则是10000
     */
    private Long tenantId;

    /**
     *  是否是官方镜像
     */
    private String isOfficial;

    /**
     *  是否启用
     */
    private String isEnabled;

    /**
     *  创建时间戳
     */
    private Date createAt;

    /**
     *  最后修改时间戳
     */
    private Date lastUpdateAt;

    /**
     *  最后修改账户ID
     */
    private String lastUpldateAccount;

}