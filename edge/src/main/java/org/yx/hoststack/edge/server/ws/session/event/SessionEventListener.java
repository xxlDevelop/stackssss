package org.yx.hoststack.edge.server.ws.session.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.yx.hoststack.edge.queue.MessageQueues;
import org.yx.hoststack.edge.server.ws.session.Session;

@Component
@RequiredArgsConstructor
public class SessionEventListener implements ApplicationListener<SessionEvent> {
    private final MessageQueues messageQueues;

    @Override
    public void onApplicationEvent(SessionEvent event) {
        if (event instanceof SessionTimeoutEvent) {
            messageQueues.getHostExitQueue().add((Session) event.getSource());
        }
    }
}
