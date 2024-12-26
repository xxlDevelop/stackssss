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
 * 已适配的云机设备信息表
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_adapted_cloud_device")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdaptedCloudDevice implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *  设备ID
     */
    private String deviceId;

    /**
     *  设备创建参数
     */
    private String initParams;

}