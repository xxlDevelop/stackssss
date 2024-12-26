package org.yx.hoststack.edge.server.ws.session;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;

@Getter
public class ContainerAgentSession extends Session {
    public ContainerAgentSession(ChannelHandlerContext context, int sessionTimeout, int sessionHbInterval) {
        super(context, sessionTimeout, sessionHbInterval, SessionType.Host);
    }

    @Override
    public void initialize() {

    }

    @Override
    public Object getSource() {
        return this;
    }
}
