package org.yx.hoststack.edge.server.ws.controller.manager;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;
import org.yx.hoststack.edge.server.ws.controller.IEdgeServerController;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.hoststack.protocol.ws.agent.common.AgentMethodId;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EdgeServerControllerManager {
    private final static Map<AgentMethodId, IEdgeServerController> CONTROLLER_MAP = new ConcurrentHashMap<>();

    public static void add(AgentMethodId agentMethodId, IEdgeServerController controller) {
        CONTROLLER_MAP.put(agentMethodId, controller);
    }

    public Optional<IEdgeServerController> get(String methodId) {
        return Optional.ofNullable(CONTROLLER_MAP.get(AgentMethodId.find(methodId)));
    }

    public void execute(IEdgeServerController controller, ChannelHandlerContext ctx, AgentCommonMessage<?> message) {
        controller.handle(ctx, message);
    }
}
