package org.yx.hoststack.center.common.req.idc;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.yx.hoststack.center.common.req.PageReq;

@Getter
@Setter
public class IdcListReq extends PageReq {

    @NotBlank
    private String zone;
    @NotBlank
    private String region;
}
