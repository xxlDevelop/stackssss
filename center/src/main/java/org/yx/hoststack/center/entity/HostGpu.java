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
 * AGENT GPU信息
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_host_gpu")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HostGpu implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     *  HOSTID
     */
    private String hostId;

    /**
     *  GPU类型
     */
    private String gpuType;

    /**
     *  制作厂商
     */
    private String gpuManufacturer;

    /**
     *  显存
     */
    private String gpuMem;

    /**
     *  BUS类型
     */
    private String gpuBusType;

    /**
     *  设备类型
     */
    private String gpuDeviceInfo;

}