package org.yx.hoststack.edge.transfer;

import io.netty.channel.ChannelHandlerContext;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;

@FunctionalInterface
public interface ITransferController {
    void handle(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage commonMessage);
}
