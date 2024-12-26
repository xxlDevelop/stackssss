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
 * 账户信息表
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_account_info")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountInfo implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *  账户ID
     */
    private Long accountId;

    /**
     *  账户所属租户ID
     */
    private Long tenantId;

    /**
     *  邮箱地址
     */
    private String email;

    /**
     *  创建时间戳
     */
    private Date createAt;

}