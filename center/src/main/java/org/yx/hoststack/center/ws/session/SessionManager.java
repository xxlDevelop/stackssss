package org.yx.hoststack.center.ws.session;

import com.google.common.collect.Maps;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.HashedWheelTimer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.common.constant.CenterCacheKeys;
import org.yx.hoststack.center.common.enums.RegisterNodeEnum;
import org.yx.hoststack.center.common.redis.util.RedissonUtils;
import org.yx.hoststack.center.ws.session.service.IdcSession;
import org.yx.hoststack.center.ws.session.service.RelaySession;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.yx.hoststack.center.common.enums.RegisterNodeEnum.IDC;
import static org.yx.hoststack.center.common.enums.RegisterNodeEnum.RELAY;

@Service
@RequiredArgsConstructor
public class SessionManager {
    private final Map<String, Session> sessionMap = Maps.newConcurrentMap();
    @Value("${applications.serverHbInterval}")
    private Integer serverHbInterval;
    private final HashedWheelTimer sessionWheelTimer = new HashedWheelTimer();

    public Session createSession(RegisterNodeEnum sessionType, ChannelHandlerContext context) {
        return switch (sessionType) {
            case IDC -> new IdcSession(IDC, context, sessionWheelTimer, serverHbInterval);
            case RELAY -> new RelaySession(RELAY, context, sessionWheelTimer, serverHbInterval);
//            case HOST -> {
//
//            }
            default -> null;
        };

    }

    public void storeSession(Session session) {
        sessionMap.put(session.getSessionId(), session);
    }

    public Optional<Session> getSession(String sessionId) {
        return Optional.ofNullable(sessionMap.get(sessionId));
    }

    public Optional<Session> getRandomSession(List<String> sessionIds) {
        for (String sid : sessionIds) {
            Session session = sessionMap.get(sid);
            if (session != null) {
                return Optional.of(session);
            }
        }
        return Optional.empty();
    }

    public <T> Optional<T> getSessionT(String sessionId) {
        return Optional.ofNullable((T) sessionMap.get(sessionId));
    }

    public String getSessionAttr(String sessionId, String attr) {
        Object cacheVal = RedissonUtils.getLocalCachedMap(String.format(CenterCacheKeys.sessionAttrInfo, sessionId)).get(attr);
        if (cacheVal != null) {
            return cacheVal.toString();
        }
        return null;
    }
}
