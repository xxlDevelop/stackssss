package org.yx.hoststack.center.entity;

import java.io.Serial;
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
 * 存储卷挂载关系
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_volume_mount_rel")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VolumeMountRel implements Serializable{

    @Serial
    private static final long serialVersionUID=1L;

    /**
     *  主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *  数据卷ID
     */
    private String volumeId;

    /**
     *  数据卷当前已经绑定的容器ID
     */
    private String mountContainerId;

    /**
     *  数据卷类型,user/base
     */
    private String volumeType;

    /**
     *  宿主机ID
     */
    private String volumeHost;

    /**
     * 挂载类型, local/net
     */
    private String mountType;

    /**
     *  挂载时间
     */
    private Date mountAt;

}