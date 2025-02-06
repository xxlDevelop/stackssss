package org.yx.hoststack.center.cache.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ServiceDetailCacheModel {

    /**
     *  主键ID
     */
    private Long id;

    /**
     *  IDCID或者中继节点ID
     */
    private String edgeId;

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
}
