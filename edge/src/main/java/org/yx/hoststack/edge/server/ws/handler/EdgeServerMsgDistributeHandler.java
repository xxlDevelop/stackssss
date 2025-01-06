package org.yx.hoststack.edge.server.ws.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.common.utils.NetUtils;
import org.yx.hoststack.edge.client.EdgeClientConnector;
import org.yx.hoststack.edge.common.ChannelType;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.server.ws.session.SessionAttrKeys;
import org.yx.hoststack.edge.server.ws.session.SessionManager;
import org.yx.hoststack.edge.forwarding.manager.ForwardingNodeMgr;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.concurrent.Executor;

@Slf4j
@Service
@ChannelHandler.Sharable
public class EdgeServerMsgDistributeHandler extends ChannelInboundHandlerAdapter {
    private final Executor executor;
    private final EdgeServerHandlerFactory edgeServerHandlerFactory;
    private final SessionManager sessionManager;
    private final ForwardingNodeMgr forwardingNodeMgr;

    public EdgeServerMsgDistributeHandler(@Qualifier("edgeExecutor") Executor executor,
                                          EdgeServerHandlerFactory edgeServerHandlerFactory,
                                          SessionManager sessionManager,
                                          ForwardingNodeMgr forwardingNodeMgr) {
        this.executor = executor;
        this.edgeServerHandlerFactory = edgeServerHandlerFactory;
        this.sessionManager = sessionManager;
        this.forwardingNodeMgr = forwardingNodeMgr;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Object clientIp = ctx.channel().attr(AttributeKey.valueOf(HostStackConstants.CLIENT_IP)).get();
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_SERVER)
                .p(LogFieldConstants.ACTION, EdgeEvent.Action.CHANNEL_ACTIVE)
                .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                .p(HostStackConstants.CLIENT_IP, clientIp)
                .d();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Object clientIp = ctx.channel().attr(AttributeKey.valueOf(HostStackConstants.CLIENT_IP)).get();
        Object channelType = ctx.channel().attr(AttributeKey.valueOf(HostStackConstants.CHANNEL_TYPE)).get();
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_SERVER)
                .p(LogFieldConstants.ACTION, EdgeEvent.Action.CHANNEL_INACTIVE)
                .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                .p(HostStackConstants.CLIENT_IP, clientIp)
                .p("ChannelType", channelType)
                .d();
        if (channelType != null && channelType.equals(ChannelType.AGENT)) {
            sessionManager.getSessionOpt(ctx.channel().id().toString()).ifPresentOrElse(
                    session -> {
                        KvLogger.instance(this)
                                .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_SERVER)
                                .p(LogFieldConstants.ACTION, EdgeEvent.Action.CHANNEL_INACTIVE)
                                .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                                .p(HostStackConstants.CLIENT_IP, clientIp)
                                .p(HostStackConstants.AGENT_ID, session.getAttr(SessionAttrKeys.AgentId))
                                .p(HostStackConstants.HOST_TYPE, session.getAttr(SessionAttrKeys.AgentType))
                                .i();
                        sessionManager.closeSession(session);
                    },
                    ctx::close
            );
        } else if (channelType != null && channelType.equals(ChannelType.IDC)) {
            Object idcSid = ctx.channel().attr(AttributeKey.valueOf(HostStackConstants.IDC_SID)).get();
            if (idcSid != null) {
                forwardingNodeMgr.get(idcSid.toString()).ifPresentOrElse(
                        transferNode -> {
                            KvLogger.instance(this)
                                    .p(LogFieldConstants.EVENT, EdgeEvent.FORWARDING_PROTOCOL)
                                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.IDC_EXIT)
                                    .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                                    .p(HostStackConstants.IDC_SID, idcSid.toString())
                                    .i();
                            EdgeClientConnector.getInstance().sendIdcExit(idcSid.toString());
                        },
                        ctx::close);
            } else {
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.FORWARDING_PROTOCOL)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.FORWARDING_TO_IDC)
                        .p(LogFieldConstants.ERR_MSG, "IdcSid is null, close")
                        .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                        .w();
                ctx.close();
            }
        } else {
            ctx.close();
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        Object clientIp = ctx.channel().attr(AttributeKey.valueOf(HostStackConstants.CLIENT_IP)).get();
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_SERVER)
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
        executor.execute(() -> edgeServerHandlerFactory.getHandler(msg).ifPresentOrElse(
                channelReadHandler -> channelReadHandler.doHandle(ctx, msg),
                () -> {
                    String clientIp = NetUtils.parseChannelRemoteAddr(ctx.channel());
                    KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_SERVER)
                            .p(LogFieldConstants.ACTION, EdgeEvent.Action.RECEIVE_MSG)
                            .p(LogFieldConstants.ERR_MSG, "UnknownSocketFrame")
                            .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                            .p(HostStackConstants.CLIENT_IP, clientIp)
                            .e();
                }));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Object clientIp = ctx.channel().attr(AttributeKey.valueOf(HostStackConstants.CLIENT_IP)).get();
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_SERVER)
                .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                .p(HostStackConstants.CLIENT_IP, clientIp)
                .e(cause);
    }
}
