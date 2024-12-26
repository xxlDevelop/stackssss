package org.yx.hoststack.center.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 租户信息
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_tenant_info")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TenantInfo implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  租户ID
     */
    private Long tenantId;

    /**
     *  租户名称
     */
    private String tenantName;

    /**
     *  租户访问HOSTSTACK的AK
     */
    private String tenantAk;

    /**
     *  租户访问HOSTSTACK的SK
     */
    private String tenantSk;

    /**
     *  创建时间戳
     */
    private Date createAt;

}