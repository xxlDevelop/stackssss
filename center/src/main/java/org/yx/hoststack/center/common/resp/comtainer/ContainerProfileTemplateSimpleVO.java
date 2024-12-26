package org.yx.hoststack.center.common.resp.comtainer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description : Simple container profile template VO
 * @Author : Lee666
 * @Date : 2023/7/10
 * @Version : 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ContainerProfileTemplateSimpleVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 5465807485886316781L;

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
     * Template Content
     */
    private String profile;
}
