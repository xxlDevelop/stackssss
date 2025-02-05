package org.yx.hoststack.edge.transfer.manager;

import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;
import org.yx.hoststack.edge.transfer.ITransferController;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RelayControllerManager {
    private final static Map<ProtoMethodId, ITransferController> CONTROLLER_MAP = new ConcurrentHashMap<>();

    public static void add(ProtoMethodId protoMethodId, ITransferController controller) {
        CONTROLLER_MAP.put(protoMethodId, controller);
    }

    public ITransferController get(int methodId) {
        return CONTROLLER_MAP.get(ProtoMethodId.find(methodId));
    }

    public void execute(ITransferController controller, ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message) {
        controller.handle(ctx, message);
    }
}
