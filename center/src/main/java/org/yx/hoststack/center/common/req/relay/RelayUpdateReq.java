package org.yx.hoststack.center.common.req.relay;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelayUpdateReq {

    @NotBlank(message = "Relay identifier cannot be empty")
    private String relay;

    @NotBlank(message = "Relay IP cannot be empty")
    private String relayIp;

    @NotBlank(message = "Net HTTPS service cannot be empty")
    private String netHttpsSvc;

    @NotBlank(message = "Net WSS service cannot be empty")
    private String netWssSvc;

    @NotBlank(message = "Location cannot be empty")
    private String location;
}