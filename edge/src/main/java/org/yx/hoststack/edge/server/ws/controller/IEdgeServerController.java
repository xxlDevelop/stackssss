package org.yx.hoststack.edge.server.ws.controller;

import io.netty.channel.ChannelHandlerContext;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;

@FunctionalInterface
public interface IEdgeServerController {
    void handle(ChannelHandlerContext ctx, AgentCommonMessage<?> agentCommonMessage);
}
