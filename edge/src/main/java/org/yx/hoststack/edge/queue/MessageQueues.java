package org.yx.hoststack.edge.queue;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.yx.hoststack.edge.queue.message.HostHeartMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@Getter
public class MessageQueues {
    private final BlockingQueue<HostHeartMessage> hostHbQueue = new LinkedBlockingQueue<>();

}
