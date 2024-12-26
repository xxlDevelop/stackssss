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
@TableName("t_coturn_config")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoturnConfig implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *   
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     *  分区标识
     */
    private String region;

    /**
     *  COTURNSERVER访问基地址
     */
    private String coturnServerSvc;

    /**
     *  COTURNSERVER访问用户名
     */
    private String coturnServerUser;

    /**
     *  COTURNSERVER访问密码
     */
    private String coturnServerPwd;

}