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
 * IDC服务或者中继节点服务信息
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_service_detail")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceDetail implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *  IDCID或者中继节点ID
     */
    private Long edgeId;

    /**
     *   
     */
    private String localIp;

    /**
     *  IDC服务版本
     */
    private String version;

    /**
     *  IDC或者RELAY
     */
    private String type;

    /**
     *  服务ID
     */
    private String serviceId;

    /**
     *  是否健康
     */
    private Byte healthy;

    /**
     *  最后心跳时间
     */
    private Date lastHbAt;

}