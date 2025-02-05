package org.yx.hoststack.center.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 存储卷
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_volume")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Volume implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  存储卷标识
     */
    private String volumeId;

    /**
     *  数据卷稀疏尺寸大小, 单位:KB
     */
    private Long volumeSize;

    /**
     *  数据卷磁盘类型, LOCAL:本地磁盘, REMOTE: 网络磁盘
     */
    private String diskType;

    /**
     *  磁盘存储所属资源的唯一标识, 本地磁盘类型时, 该字段是BAREMETAL资源ID, 网络磁盘时, 该字段是分布式存储系统CEPH
     */
    private String volumeHost;

    /**
     *  创建时间戳
     */
    private Date createAt;

}