package org.yx.hoststack.center.entity;

import java.io.Serializable;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 区域信息
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_region_info")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegionInfo implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *  分区CODE
     */
    private String regionCode;

    /**
     *  大区
     */
    private String zoneCode;

    /**
     *  分区公网IP，一个分区可以支持IDC机房，或者中继节点
     */
    private String publicIpList;

    /**
     *  分区GPS信息
     */
    private String location;

    /**
     *  COTURN服务地址
     */
    private String coturnSvc;

    /**
     *  COTURN服务用户名
     */
    private String coturnUser;

    /**
     *  COTURN服务密码
     */
    private String coturnPwd;

    /**
     *  对象存储类型,OSS/COS/S3
     */
    private String ossType;

    /**
     *  对象存储配置
     */
    private String ossConfig;

}