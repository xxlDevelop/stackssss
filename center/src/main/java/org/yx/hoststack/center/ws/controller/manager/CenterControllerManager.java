package org.yx.hoststack.center.ws.controller.manager;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;
import org.yx.hoststack.center.ws.controller.IServerController;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CenterControllerManager {
    private final static Map<ProtoMethodId, IServerController> CONTROLLER_MAP = new ConcurrentHashMap<>();

    public static void add(ProtoMethodId protoMethodId, IServerController controller) {
        CONTROLLER_MAP.put(protoMethodId, controller);
    }

    public IServerController get(int methodId) {
        return CONTROLLER_MAP.get(ProtoMethodId.find(methodId));
    }

    public void execute(IServerController controller, ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message) {
        controller.handle(ctx, message);
    }
}
