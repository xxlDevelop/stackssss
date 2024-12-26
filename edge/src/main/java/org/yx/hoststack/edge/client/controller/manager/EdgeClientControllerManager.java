package org.yx.hoststack.edge.client.controller.manager;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;
import org.yx.hoststack.edge.client.controller.IEdgeClientController;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EdgeClientControllerManager {
    private final static Map<ProtoMethodId, IEdgeClientController> CONTROLLER_MAP = new ConcurrentHashMap<>();

    public static void add(ProtoMethodId protoMethodId, IEdgeClientController controller) {
        CONTROLLER_MAP.put(protoMethodId, controller);
    }

    public Optional<IEdgeClientController> get(int methodId) {
        return Optional.ofNullable(CONTROLLER_MAP.get(ProtoMethodId.find(methodId)));
    }

    public void execute(IEdgeClientController controller, ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message) {
        controller.handle(ctx, message);
    }
}
