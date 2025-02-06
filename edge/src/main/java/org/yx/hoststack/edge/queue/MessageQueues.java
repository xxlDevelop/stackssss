package org.yx.hoststack.edge.queue;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.yx.hoststack.edge.queue.message.HostHeartMessage;
import org.yx.hoststack.edge.server.ws.session.Session;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@Getter
public class MessageQueues {
    private final BlockingQueue<HostHeartMessage> hostHbQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<AgentCommonMessage<?>> jobNotifyToDiskQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<AgentCommonMessage<?>> jobNotifyToCenterQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Session> hostExitQueue = new LinkedBlockingQueue<>();
}
