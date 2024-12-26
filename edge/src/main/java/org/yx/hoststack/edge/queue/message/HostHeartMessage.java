package org.yx.hoststack.edge.queue.message;

import lombok.Builder;
import lombok.Data;
import org.yx.hoststack.protocol.ws.agent.req.HostHeartbeatReq;

@Data
@Builder
public class HostHeartMessage {
    private String hostId;
    private String agentType;
    private HostHeartbeatReq hostHeartbeatReq;
}
