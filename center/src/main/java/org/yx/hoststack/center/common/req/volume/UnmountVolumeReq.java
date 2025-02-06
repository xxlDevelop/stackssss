package org.yx.hoststack.center.common.req.volume;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UnmountVolumeReq {
    private String zone;
    private String region;
    private String relay;
    private String idc;

    @NotBlank(message = "volumeId cannot be empty")
    private String volumeId;

    @NotBlank(message = "cid cannot be empty")
    private String cid;
}