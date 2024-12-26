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
 *  
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_crm_access_info")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrmAccessInfo implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *  BAREMETAL提供者ID
     */
    private Long baremetalProvider;

    /**
     *  租户ID
     */
    private Long tenantId;

    /**
     *   
     */
    private String ak;

    /**
     *   
     */
    private String sk;

    /**
     *  创建时间戳
     */
    private Date createAt;

}