package org.yx.hoststack.edge.client.controller;

import io.netty.channel.ChannelHandlerContext;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;

@FunctionalInterface
public interface IEdgeClientController {
    void handle(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage commonMessage);
}
