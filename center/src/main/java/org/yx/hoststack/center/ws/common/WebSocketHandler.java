package org.yx.hoststack.center.ws.common;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketHandler extends ChannelInboundHandlerAdapter {

    private static final ConcurrentHashMap<Channel, String> channelIpMap = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String serviceIp = getServiceIpFromContext(ctx);
        channelIpMap.put(ctx.channel(), serviceIp);

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String serviceIp = channelIpMap.get(ctx.channel());
        if (serviceIp != null) {
            ConnectionManager.removeConnection(serviceIp);
            channelIpMap.remove(ctx.channel());
        }

        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    // 获取服务 IP 地址（假设可以从 ctx 或 msg 中提取）
    private String getServiceIpFromContext(ChannelHandlerContext ctx) {
        return ctx.channel().remoteAddress().toString().split(":")[0].substring(1);
    }
}
