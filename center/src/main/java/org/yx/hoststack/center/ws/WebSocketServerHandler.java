package org.yx.hoststack.center.ws;

import cn.hutool.core.map.MapBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.ws.controller.manager.CenterControllerManager;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.HashMap;
import java.util.concurrent.Executor;

@Service
@ChannelHandler.Sharable
public class WebSocketServerHandler extends ChannelInboundHandlerAdapter {
    private final CenterControllerManager centerControllerManager;
    private final Executor executor;

    public WebSocketServerHandler(CenterControllerManager centerControllerManager,
                                  @Qualifier("centerExecutor") Executor executor) {
        this.centerControllerManager = centerControllerManager;
        this.executor = executor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            Object channelClientId = "";
//            if (channelClientId == null) {
//                log.warn("channelClientId is null, channelId:{}, remoteAddr:{}, to closed", ctx.channel().id(), ctx.channel().remoteAddress());
//                channelManager.closeChannel(ctx);
//                return;
//            }
//            ChannelManager.ClientChannel clientChannel = channelManager.get(channelClientId.toString());
//            if (clientChannel == null) {
//                log.info("client not found clientId:{}, channelId:{}, to be close", channelClientId, ctx.channel().id());
//                channelManager.closeChannel0(ctx);
//                return;
//            }
//            if (!clientChannel.getChannel().id().equals(ctx.channel().id())) {
//                log.info("client channel not match current, clientId:{}, curChannelId:{}, savedChannelId:{}, close curChannel",
//                        channelClientId, ctx.channel().id(), clientChannel.getChannel().id());
//                channelManager.closeChannel0(ctx);
//                return;
//            }
            if (msg instanceof BinaryWebSocketFrame) {
                ByteBuf byteBuf = ((BinaryWebSocketFrame) msg).content();
                byte[] contentBytes = ByteBufUtil.getBytes(byteBuf);
                CommonMessageWrapper.CommonMessage commonMessage = CommonMessageWrapper.CommonMessage.parseFrom(contentBytes);
                executor.execute(() -> {
                    String clientIp = NettyUtils.parseChannelRemoteAddr(ctx.channel());
                    TraceHolder.stopWatch(MapBuilder.create(new HashMap<String, String>())
                                    .put(LogFieldConstants.EVENT, CenterEvent.CenterWsServer)
                                    .put(LogFieldConstants.ACTION, CenterEvent.Action.CenterWsServer_ReceiveMsg)
                                    .put(HostStackConstants.CHANNEL_ID, ctx.channel().id().toString())
                                    .put(HostStackConstants.METH_ID, commonMessage.getHeader().getMethId() + "")
                                    .put(HostStackConstants.TRACE_ID, commonMessage.getHeader().getTraceId())
                                    .put(HostStackConstants.CLIENT_IP, clientIp)
                                    .build(),
                            () -> centerControllerManager.get(commonMessage.getHeader().getMethId()).handle(ctx, commonMessage));
                });
            }
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.CenterWsServer)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.CenterWsServer_ReceiveMsg)
                    .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                    .e(e);
//            channelManager.closeChannel(ctx);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}

