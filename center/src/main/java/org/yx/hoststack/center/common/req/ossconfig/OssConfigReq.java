package org.yx.hoststack.center.common.req.ossconfig;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Data
public class OssConfigReq implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;

    @NotBlank(message = "region cannot be empty")
    private String region;

    @NotBlank(message = "ossType cannot be empty")
    private String ossType;

    @NotBlank(message = "ossConfig cannot be empty")
    private String ossConfig;

}