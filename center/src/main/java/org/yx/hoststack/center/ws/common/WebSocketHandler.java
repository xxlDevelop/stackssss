package org.yx.hoststack.center.ws.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.yx.hoststack.center.CenterApplicationRunner;
import org.yx.hoststack.center.ws.CenterServer;
import org.yx.hoststack.center.ws.controller.EdgeController;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.lib.utils.util.SpringContextHolder;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static org.yx.hoststack.center.ws.CenterServer.centerNode;
import static org.yx.hoststack.center.ws.common.Node.NODE_MAP;
import static org.yx.hoststack.center.ws.controller.EdgeController.serverDetailCacheMap;

@Component
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class WebSocketHandler extends ChannelInboundHandlerAdapter {
    private final EdgeController edgeController;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Object serviceId = ctx.channel().attr(AttributeKey.valueOf("innerServiceId")).get();
        edgeController.offline(String.valueOf(serviceId));
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ctx.fireChannelRead(msg);
    }
}
