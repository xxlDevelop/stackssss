package org.yx.hoststack.center.common.req.container;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description : Container profile template  page request DTO
 * @Author : Lee666
 * @Date : 2024/12/24
 * @Version : 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class ContainerProfileTemplatePageReqDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -990658096757269595L;
    /**
     * The business types applicable to the image: render/ai
     */
    private String bizType;
    /**
     * Mirror operating system type: windows/linux/android
     */
    private String osType;
    /**
     * Mirror virtualization type: docker/kvm
     */
    private String containerType;
    /**
     * Hardware architecture: arm/x86
     */
    private String arch;
    /**
     * Current page index, default 1
     */
    private Long current;
    /**
     * Display rows per page, default is 20
     */
    private Long size;
}
