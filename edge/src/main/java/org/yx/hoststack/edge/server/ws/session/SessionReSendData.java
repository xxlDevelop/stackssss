package org.yx.hoststack.edge.server.ws.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.yx.hoststack.protocol.ws.agent.common.CommonMessage;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionReSendData {

    private int centerMethId;
    private CommonMessage<?> message;
}
