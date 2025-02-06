package org.yx.hoststack.center.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 区域信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_region_info")
public class RegionInfo {
    /**
     * 分区Code
     */
    private String regionCode;

    /**
     * 分区ID
     */
    private Integer regionId;

    /**
     * 大区
     */
    private String zoneCode;

    /**
     * 分区公网IP，一个分区可以支持IDC机房，或者中继节点
     */
    private String publicIpList;

    /**
     * 分区GPS信息
     */
    private String location;
}