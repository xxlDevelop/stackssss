package org.yx.hoststack.center.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件提取打包信息
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_package_files_info")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PackageFilesInfo implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *   
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *  打包的文件ID
     */
    private String pkgId;

    /**
     *  打包文件的MD5
     */
    private String pkgMd5;

    /**
     *  打包后的文件大小, 单位KB
     */
    private Long fileSize;

    /**
     *  存储类型, HOST:存储在宿主机, CLOUD:存储在云端存储服务
     */
    private String storeType;

    /**
     *  需要提取文件的容器ID
     */
    private String containerId;

    /**
     *  创建时间
     */
    private Date createAt;

}