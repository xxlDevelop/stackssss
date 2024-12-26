package org.yx.hoststack.edge.server.ws.session.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;


@Getter
public abstract class SessionEvent extends ApplicationEvent {
    private final Object eventData;

    public SessionEvent(Object source, Object eventData) {
        super(source);
        this.eventData = eventData;
    }
}
