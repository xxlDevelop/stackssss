package org.yx.hoststack.center.ws.session.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.yx.hoststack.center.ws.session.Session;

@Component
@RequiredArgsConstructor
public class SessionEventListener implements ApplicationListener<SessionEvent> {

    @Override
    public void onApplicationEvent(SessionEvent event) {
        if (event instanceof SessionTimeoutEvent) {
            // TODO need queues to destroy
            Session session = (Session) event.getSource();
            session.destroy();
        }
    }
}
