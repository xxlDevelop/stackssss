package org.yx.hoststack.center.common.req.volume;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MountVolumeReq {
    private String zone;
    private String region;
    private String relay;
    private String idc;

    @NotBlank(message = "volumeId cannot be empty")
    private String volumeId;

    private String baseVolumeId;

    @NotBlank(message = "cid cannot be empty")
    private String cid;
}