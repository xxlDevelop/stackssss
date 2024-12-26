package org.yx.hoststack.edge.server.ws.session.event;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.yx.hoststack.edge.server.ws.session.Session;
import org.yx.hoststack.edge.server.ws.session.SessionManager;

@Component
@RequiredArgsConstructor
public class SessionEventListener implements ApplicationListener<SessionEvent> {
    private final SessionManager sessionManager;

    @Override
    public void onApplicationEvent(@NotNull SessionEvent event) {
        if (event instanceof SessionTimeoutEvent) {
            sessionManager.closeSession((Session) event.getEventData());
        }
    }
}
