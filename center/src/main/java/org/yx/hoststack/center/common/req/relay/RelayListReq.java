package org.yx.hoststack.center.common.req.relay;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.yx.hoststack.center.common.req.PageReq;

@Data
public class RelayListReq extends PageReq {
    /**
     * Zone identifier
     */
    @NotBlank(message = "Zone identifier cannot be empty")
    @Size(max = 45, message = "Zone identifier length cannot exceed 45 characters")
    private String zone;
    
    /**
     * Region identifier
     */
    @NotBlank(message = "Region identifier cannot be empty")
    @Size(max = 45, message = "Region identifier length cannot exceed 45 characters")
    private String region;
}