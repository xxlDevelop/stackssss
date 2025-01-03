package org.yx.hoststack.edge.server.ws.session;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.HashedWheelTimer;
import lombok.Getter;

@Getter
public class ContainerAgentSession extends Session {
    public ContainerAgentSession(ChannelHandlerContext context, String sessionId, int sessionTimeout, HashedWheelTimer hashedWheelTimer) {
        super(context, sessionId, sessionTimeout, SessionType.Host, hashedWheelTimer);
    }

    @Override
    public void initialize() {

    }

    @Override
    public Object getSource() {
        return this;
    }
}
