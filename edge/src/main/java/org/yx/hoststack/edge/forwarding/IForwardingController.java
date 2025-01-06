package org.yx.hoststack.edge.forwarding;

import io.netty.channel.ChannelHandlerContext;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;

@FunctionalInterface
public interface IForwardingController {
    void handle(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage commonMessage);
}
