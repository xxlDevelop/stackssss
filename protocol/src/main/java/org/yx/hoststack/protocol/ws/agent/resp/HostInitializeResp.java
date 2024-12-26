package org.yx.hoststack.protocol.ws.agent.resp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HostInitializeResp {

    /**
     * hostId : 1234567890
     */
    private String hostId;
}
