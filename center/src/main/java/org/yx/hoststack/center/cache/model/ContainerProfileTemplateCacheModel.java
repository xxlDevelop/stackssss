package org.yx.hoststack.center.cache.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContainerProfileTemplateCacheModel {
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
     *  Template Content
     */
    private String profile;

    /**
     *  create time
     */
    private Date createAt;
}
