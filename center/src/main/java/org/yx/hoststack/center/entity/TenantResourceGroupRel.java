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
 * 资源分组与资源绑定关系表
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_tenant_resource_group_rel")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantResourceGroupRel implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *  资源分组ID
     */
    private Long resourceGroupId;

    /**
     *  资源ID
     */
    private String resourceId;

    /**
     *  资源所属的HOSTID
     */
    private String hostId;

    /**
     *  资源类型，BM/CONTAINER
     */
    private String resourceType;

}