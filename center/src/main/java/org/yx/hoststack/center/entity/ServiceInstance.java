package org.yx.hoststack.center.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * idc服务或者中继节点服务实例信息
 * </p>
 *
 * @author Lee666
 * @since 2024-12-25
 */
@TableName("t_service_instance")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceInstance implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * idcId或者中继节点id
     */
    private Long edgeId;

    private String localIp;

    /**
     * idc服务版本
     */
    private String version;

    /**
     * idc或者relay
     */
    private String type;

    /**
     * 服务ID
     */
    private String serviceId;

    /**
     * 是否健康
     */
    private Boolean healthy;

    /**
     * 最后心跳时间
     */
    private Date lastHbAt;

}
