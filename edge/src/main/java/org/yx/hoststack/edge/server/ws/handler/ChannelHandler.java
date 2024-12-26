package org.yx.hoststack.edge.server.ws.handler;


import io.netty.channel.ChannelHandlerContext;

public interface ChannelHandler {
    void doHandle(ChannelHandlerContext ctx, Object msg);
}
