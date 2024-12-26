package org.yx.hoststack.edge.server.ws.session.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionEventPublisher {
    private final ApplicationEventPublisher publisher;

    public void publishCustomEvent(SessionEvent sessionEvent) {
        publisher.publishEvent(sessionEvent);
    }
}
