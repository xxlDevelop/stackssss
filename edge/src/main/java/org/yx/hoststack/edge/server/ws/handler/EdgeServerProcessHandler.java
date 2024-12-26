package org.yx.hoststack.edge.server.ws.handler;

import cn.hutool.core.map.MapBuilder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.yx.hoststack.common.utils.NetUtils;
import org.yx.hoststack.common.TraceHolder;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.server.ws.session.SessionManager;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.HashMap;
import java.util.concurrent.Executor;

@Slf4j
@Service
@ChannelHandler.Sharable
public class EdgeServerProcessHandler extends ChannelInboundHandlerAdapter {
    private final Executor executor;
    private final EdgeServerHandlerFactory edgeServerHandlerFactory;
    private final SessionManager sessionManager;

    public EdgeServerProcessHandler(@Qualifier("edgeExecutor") Executor executor,
                                    EdgeServerHandlerFactory edgeServerHandlerFactory,
                                    SessionManager sessionManager) {
        this.executor = executor;
        this.edgeServerHandlerFactory = edgeServerHandlerFactory;
        this.sessionManager = sessionManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Object clientIp = ctx.channel().attr(AttributeKey.valueOf(HostStackConstants.CLIENT_IP)).get();
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                .p(LogFieldConstants.ACTION, EdgeEvent.Action.Channel_Active)
                .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                .p(HostStackConstants.CLIENT_IP, clientIp)
                .d();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Object clientIp = ctx.channel().attr(AttributeKey.valueOf(HostStackConstants.CLIENT_IP)).get();
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                .p(LogFieldConstants.ACTION, EdgeEvent.Action.Channel_Inactive)
                .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                .p(HostStackConstants.CLIENT_IP, clientIp)
                .d();
        sessionManager.getSessionOpt(ctx.channel().id().toString()).ifPresentOrElse(
                sessionManager::closeSession,
                () -> ctx.channel().close()
        );
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Object clientIp = ctx.channel().attr(AttributeKey.valueOf(HostStackConstants.CLIENT_IP)).get();
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                .p(LogFieldConstants.ACTION, EdgeEvent.Action.HANDLER_REMOVED)
                .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                .p(HostStackConstants.CLIENT_IP, clientIp)
                .d();
    }

//    @Override
//    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        if (evt instanceof IdleStateEvent) {
//            IdleStateEvent event = (IdleStateEvent) evt;
//            if (event == IdleStateEvent.READER_IDLE_STATE_EVENT) {
//                Object clientId = ctx.channel().attr(AttributeKey.valueOf(MsgCenterConstants.CHANNEL_CLIENT_ID)).get();
//                log.info("[userEventTriggered] server read idle from client, clientId:{}, remoteAddr: {}", clientId, ctx.channel().remoteAddress());
//                channelManager.closeChannel(clientId, ctx);
//            }
//
//            if (event == IdleStateEvent.ALL_IDLE_STATE_EVENT) {
//                  Object clientId = ctx.channel().attr(AttributeKey.valueOf(EdgeConstants.CHANNEL_ATTR_CLIENT_ID)).get();
//                Object clientId = "";
//                channelManager.closeChannel(clientId, ctx.channel());
//                log.info("client read write Idle, clientId:{} channelId:{}, remoteAddr: {}, closed", clientId, ctx.channel().id(), ctx.channel().remoteAddress());
//            }
//        }
//    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        executor.execute(() -> {
            String clientIp = NetUtils.parseChannelRemoteAddr(ctx.channel());
            TraceHolder.stopWatch(MapBuilder.create(new HashMap<String, String>())
                            .put(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                            .put(LogFieldConstants.ACTION, EdgeEvent.Action.ReceiveMsg)
                            .put(HostStackConstants.CHANNEL_ID, ctx.channel().id().toString())
                            .put(HostStackConstants.CLIENT_IP, clientIp)
                            .build(),
                    () -> edgeServerHandlerFactory.getHandler(msg).ifPresentOrElse(
                            channelReadHandler -> channelReadHandler.doHandle(ctx, msg),
                            () -> {
                                KvLogger.instance(this)
                                        .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.ReceiveMsg)
                                        .p(LogFieldConstants.ERR_MSG, "UnknownSocketFrame")
                                        .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                                        .p(HostStackConstants.CLIENT_IP, clientIp)
                                        .e();
                            }));
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Object clientIp = ctx.channel().attr(AttributeKey.valueOf(HostStackConstants.CLIENT_IP)).get();
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                .p(LogFieldConstants.ACTION, EdgeEvent.Action.HANDLER_REMOVED)
                .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                .p(HostStackConstants.CLIENT_IP, clientIp)
                .e(cause);
    }
}
