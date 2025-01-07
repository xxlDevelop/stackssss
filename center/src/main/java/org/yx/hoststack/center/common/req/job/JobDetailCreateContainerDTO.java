package org.yx.hoststack.center.common.req.job;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description : JonDetail  create Container DTO
 * @Author : Lee666
 * @Date : 2025/1/4
 * @Version : 1.0
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class JobDetailCreateContainerDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -5621420962129112239L;
    /**
     * cid
     */
    private String containerId;
    /**
     * Host resource ID to which the container belongs, created through the hoststack platform, this field is not null
     */
    private String hostId;
    /**
     * Self increasing serial number; Define a unique primary key in combination with HostId
     */
    private Integer sequenceNumber;
    /**
     * Image ID, created through the hoststack platform, this field is not null
     */
    private String imageId;
    /**
     * Image version, created through the hoststack platform, this field is not null
     */
    private String imageVer;
    /**
     * Container IP address
     */
    private String containerIp;
    /**
     * Operating system type
     */
    private String osType;
    /**
     * Number of CPU cores
     */
    private Integer vCpu;
    /**
     * Memory, unit MB
     */
    private String memory;
    /**
     * Network card working mode: bridge、nat
     */
    private String netMode;
}
