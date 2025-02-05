package org.yx.hoststack.center.common.req.idc;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdcListReq {

    @NotBlank
    private String zone;
    @NotBlank
    private String region;
}
