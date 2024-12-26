package org.yx.hoststack.center.ws.controller;

import io.netty.channel.ChannelHandlerContext;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;

@FunctionalInterface
public interface IServerController {
    void handle(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage commonMessage);
}
