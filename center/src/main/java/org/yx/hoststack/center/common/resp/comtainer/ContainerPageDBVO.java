package org.yx.hoststack.center.common.resp.comtainer;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @Description : Simple container profile template VO
 * @Author : Lee666
 * @Date : 2023/7/10
 * @Version : 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ContainerPageDBVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -7455040693462310398L;
    /**
     * Container ID
     */
    private String cid;
    /**
     * Host ID
     */
    private String hostId;
    /**
     * Zone identifier to which the container belongs
     */
    private String zone;
    /**
     * Region identifier to which the container belongs
     */
    private String region;
    /**
     * Image ID, created through the hoststack platform, this field is not null
     */
    private String imageId;
    /**
     * Image version, created through the hoststack platform, this field is not null
     */
    private String imageVer;
    /**
     *  Image name
     */
    private String imageName;
    /**
     * Operating system type
     */
    private String osType;
    /**
     * Resource pool type, idc, edge, he
     */
    private String resourcePool;
    /**
     * The business types applicable to the image, render/ai
     */
    private String bizType;
    /**
     * Mirror virtualization type: docker/kvm
     */
    private String containerType;
    /**
     * Device serial number
     */
    private String devSn;
    /**
     * Operating system memory
     */
    private String osMem;
    /**
     * Current operating system virtualization information: bm (bare metal), docker, vm
     */
    private String runtimeEnv;
    /**
     *  label
     */
    private String label;
    /**
     *  If the tenant ID to which the image belongs is uploaded by the administrator, TID is 10000
     */
    private Long tenantId;
    /**
     * Creation timestamp
     */
    private Date createAt;

}
