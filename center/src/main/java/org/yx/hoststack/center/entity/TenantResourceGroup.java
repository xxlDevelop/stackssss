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
 * 资源分组表
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_tenant_resource_group")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantResourceGroup implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *  租户ID
     */
    private Long tenantId;

    /**
     *  租户下单的订单号
     */
    private String orderCode;

    /**
     *  订单过期时间
     */
    private Date expireAt;

    /**
     *  创建时间
     */
    private Date createAt;

}