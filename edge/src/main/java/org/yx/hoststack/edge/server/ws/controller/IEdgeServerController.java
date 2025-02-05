package org.yx.hoststack.edge.server.ws.controller;

import io.netty.channel.ChannelHandlerContext;
import org.yx.hoststack.protocol.ws.agent.common.CommonMessage;

@FunctionalInterface
public interface IEdgeServerController {
    void handle(ChannelHandlerContext ctx, CommonMessage<?> commonMessage);
}
