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
public class ContainerSimpleVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 5465807485886316781L;

    /**
     * Image ID, created through the hoststack platform, this field is not null
     */
    private String imageId;

    /**
     * Image version, created through the hoststack platform, this field is not null
     */
    private String imageVer;

    /**
     * Operating system type
     */
    private String osType;

    /**
     * Resource pool type, idc, edge, he
     */
    private String resourcePool;


    /**
     * Creation timestamp
     */
    private Long createAt;

}
