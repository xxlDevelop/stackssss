package org.yx.hoststack.center.common.req.container;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description : Container page request DTO
 * @Author : Lee666
 * @Date : 2024/12/24
 * @Version : 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class ContainerPageReqDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 2890191505467821497L;

    /**
     * Zone identifier to which the container belongs
     */
    private String zone;

    /**
     * Region identifier to which the container belongs
     */
    private String region;
    /**
     * Container resource pool type EDGE/IDC
     */
    private String resourcePool;
    /**
     * Container operating system type, WINDOWS, LINUX, ANDROID
     */
    private String osType;
    /**
     * The business types applicable to the image, render/ai
     */
    private String bizType;
    /**
     * Mirror virtualization type: docker/kvm
     */
    private String containerType;
    /**
     * label
     */
    private String label;
    /**
     * Is it an official image? Default false
     */
    private Boolean isOfficial;
    /**
     * Is the image available? Default is false
     */
    private Boolean isEnabled;


    /**
     * Current page index, default 1
     */
    private Long current;
    /**
     * Display rows per page, default is 20
     */
    private Long size;
}
