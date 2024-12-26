package org.yx.hoststack.protocol.ws.agent.req;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BmkHeartbeatReq {
    private String benchmarkId;
}
