package org.yx.hoststack.center.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 机房信息
 *
 * @author lyc
 * @since 2024-12-12 18:09:53
 */
@Data
@TableName("t_idc_info")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IdcInfo implements Serializable{

    private static final long serialVersionUID=1L;

    /**
     *   
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     *  大区标识
     */
    private String zone;

    /**
     *  分区标识
     */
    private String region;

    /**
     *  机房标识
     */
    private String idc;

    /**
     *  IDC机房公网IP
     */
    private String idcIp;

    /**
     *  HOST-STACK-IDC, 内网HTTP访问基地址
     */
    private String localHsIdcHttpSvc;

    /**
     *  HOST-STACK-IDC, 公网HTTPS访问基地址
     */
    private String netHsIdcHttpsSvc;

    /**
     *  HOST-STACK-IDC, 内网WS访问基地址
     */
    private String localHsIdcWsSvc;

    /**
     *  共享存储 内网HTTP访问基地址
     */
    private String localShareStorageHttpSvc;

    /**
     *  共享存储访问用户名
     */
    private String shareStorageUser;

    /**
     *  共享存储访问密码
     */
    private String shareStoragePwd;

    /**
     *  LOGSVC内网服务地址
     */
    @TableField(value = "local_logsvc_http_svc")
    private String localLogSvcHttpSvc;

    /**
     *  LOGSVC公网服务地址
     */
    @TableField(value = "net_logsvc_https_svc")
    private String netLogSvcHttpsSvc;

    /**
     *  测速地址
     */
    private String speedTestSvc;

    /**
     *  机房GPS坐标
     */
    private String location;

    /**
     *  创建时间
     */
    private Date createAt;

    /**
     *  最后修改时间
     */
    private Date lastUpdateAt;

}