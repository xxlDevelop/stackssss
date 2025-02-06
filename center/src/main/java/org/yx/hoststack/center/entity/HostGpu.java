package org.yx.hoststack.center.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HostAgent GPU信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_host_gpu")
public class HostGpu {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * hostId
     */
    private String hostId;

    /**
     * gpu类型
     */
    private String gpuType;

    /**
     * 制作厂商
     */
    private String gpuManufacturer;

    /**
     * 显存
     */
    private String gpuMem;

    /**
     * bus类型
     */
    private String gpuBusType;

    /**
     * gpu设备ID
     */
    private String gpuDeviceId;

    /**
     * linux宿主机显卡的PCIe 设备地址，用于显卡直通时指定虚拟机使用的显卡
     */
    private String gpuBusId;
}