package org.yx.hoststack.center.common.req.volume;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class DeleteVolumeReq {
    private String zone;
    private String region;
    private String relay;
    private String idc;
    
    @NotEmpty(message = "volumeIds cannot be empty")
    private List<String> volumeIds;
}