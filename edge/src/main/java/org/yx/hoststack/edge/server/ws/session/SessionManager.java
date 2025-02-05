package org.yx.hoststack.edge.server.ws.session;

import com.google.common.collect.Maps;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Service;
import org.yx.hoststack.edge.client.EdgeClientConnector;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SessionManager {
    private final Map<String, Session> sessionMap = Maps.newHashMap();


    /**
     * create session
     * @param context               ChannelHandlerContext
     * @param sessionType           Host or Container
     * @param sessionTimeout        SessionTimeout
     * @param sessionHbInterval     SessionHbInterval
     * @return Session
     */
    public Session createSession(ChannelHandlerContext context, SessionType sessionType, int sessionTimeout, int sessionHbInterval) {
        String sessionId = context.channel().id().toString();
        Session session = sessionMap.get(sessionId);
        if (session == null) {
            if (sessionType == SessionType.Host) {
                session = new HostAgentSession(context, sessionTimeout, sessionHbInterval);
            } else {
                session = new ContainerAgentSession(context, sessionTimeout, sessionHbInterval);
            }
            session.initialize0();
            sessionMap.put(sessionId, session);
        }
        return session;
    }

    public Session getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

    public Optional<Session> getSessionOpt(String sessionId) {
        return Optional.ofNullable(sessionMap.get(sessionId));
    }

    public void closeSession(Session target) {
        Session session = sessionMap.remove(target.getSessionId());
        if (session != null) {
            target.destroy();
            sessionMap.remove(target.getSessionId());
            // send host exit
            String hostId = session.getAttr(SessionAttrKeys.AgentId).toString();
            String agentType = session.getAttr(SessionAttrKeys.AgentType).toString();
            EdgeClientConnector.getInstance().sendHostExit(hostId, agentType);
        }
    }

    public List<Session> getSessions(SessionType sessionType) {
        return sessionMap.values().stream().filter(session -> session.getSessionType() == sessionType).collect(Collectors.toList());
    }

    public void closeAll() {
        sessionMap.values().forEach(Session::destroy);
        sessionMap.clear();
    }
}
