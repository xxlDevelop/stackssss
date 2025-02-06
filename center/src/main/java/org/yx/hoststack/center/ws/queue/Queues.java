package org.yx.hoststack.center.ws.queue;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.yx.hoststack.center.ws.session.Session;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@Getter
public class Queues {
    private final BlockingQueue<Long> serviceRefreshHealthQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Session> serviceRegisterQueue = new LinkedBlockingQueue<>();
}
