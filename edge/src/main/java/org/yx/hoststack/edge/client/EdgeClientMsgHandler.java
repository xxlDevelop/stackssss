package org.yx.hoststack.edge.client;

import cn.hutool.core.map.MapBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.common.utils.NetUtils;
import org.yx.hoststack.common.TraceHolder;
import org.yx.hoststack.edge.client.controller.manager.EdgeClientControllerManager;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.transfer.manager.RelayControllerManager;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.SpringContextHolder;
import org.yx.lib.utils.util.StringUtil;

import java.util.HashMap;
import java.util.concurrent.Executor;

@Slf4j
@Service
@ChannelHandler.Sharable
public class EdgeClientMsgHandler extends ChannelInboundHandlerAdapter {
    private final Executor executorService;
    private final EdgeClientControllerManager edgeClientControllerManager;
    private final RelayControllerManager relayControllerManager;

    public EdgeClientMsgHandler(@Qualifier("edgeExecutor") Executor executorService,
                                EdgeClientControllerManager edgeClientControllerManager,
                                RelayControllerManager relayControllerManager) {
        this.executorService = executorService;
        this.edgeClientControllerManager = edgeClientControllerManager;
        this.relayControllerManager = relayControllerManager;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (SpringContextHolder.getApplicationContext() != null) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                    .p(LogFieldConstants.EVENT, EdgeEvent.Action.EdgeWsClient_CloseByServer)
                    .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                    .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                    .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                    .p(HostStackConstants.REGION, EdgeContext.Region)
                    .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                    .i();
            SpringContextHolder.getBean(EdgeClient.class).reConnect();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE.equals(evt)) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                    .p(LogFieldConstants.EVENT, EdgeEvent.Action.EdgeWsClient_HandshakeSuccessful)
                    .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                    .i();
            ClientWaitConnectSignal.release();
            // 握手成功，发送edge注册消息
        } else if (WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_TIMEOUT.equals(evt)) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                    .p(LogFieldConstants.EVENT, EdgeEvent.Action.EdgeWsClient_HandshakeTimeout)
                    .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                    .i();
            ClientWaitConnectSignal.release();
        }
//        else if (evt instanceof IdleStateEvent) {
//            IdleStateEvent event = (IdleStateEvent) evt;
//            if (event == IdleStateEvent.ALL_IDLE_STATE_EVENT) {
//                log.info("client read write Idle, clientId:{} channelId:{}, remoteAddr: {}, closed", clientId, ctx.channel().id(), ctx.channel().remoteAddress());
//            }
//        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof BinaryWebSocketFrame) {
            executorService.execute(() -> {
                try {
                    String clientIp = NetUtils.parseChannelRemoteAddr(ctx.channel());
                    ByteBuf byteBuf = ((BinaryWebSocketFrame) msg).content();
                    byte[] contentBytes = ByteBufUtil.getBytes(byteBuf);
                    CommonMessageWrapper.CommonMessage commonMessage = CommonMessageWrapper.CommonMessage.parseFrom(contentBytes);
                    if (commonMessage.getHeader().getLinkSide() != CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE) {
                        KvLogger.instance(this)
                                .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                                .p(LogFieldConstants.ACTION, EdgeEvent.Action.ReceiveMsg)
                                .p(LogFieldConstants.TRACE_ID, commonMessage.getHeader().getTraceId())
                                .p(LogFieldConstants.ERR_MSG, "LinkSide not match, Not ServerToClient")
                                .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                                .p(HostStackConstants.TRACE_ID, commonMessage.getHeader().getTraceId())
                                .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                                .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                                .p(HostStackConstants.REGION, EdgeContext.Region)
                                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                                .p(HostStackConstants.CLIENT_IP, clientIp)
                                .w();
                        ReferenceCountUtil.release(msg);
                        return;
                    }
//            if (commonMessage.getHeader()getZone() == null || commonMessage.getHeader().getRegion() == null ||
//                    (!commonMessage.getHeader().getZone().equals(commonConfig.getZone()) &&
//                    !commonMessage.getHeader().getRegion().equals(commonConfig.getRegion()))) {
//                KvLogger.instance(this)
//                        .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
//                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.EdgeWsClient_Receive)
//                        .p(LogFieldConstants.ERR_MSG, "Zone or Region not match")
//                        .p(LogFieldConstants.TRACE_ID, commonMessage.getHeader().getTraceId())
//                        .p(EdgeConstants.CHANNEL_ATTR_CHANNEL_ID, ctx.channel().id())
//                        .p(EdgeConstants.MSG_ID, commonMessage.getBody().getMsgId())
//                        .p("ReceiveZone", commonMessage.getHeader().getZone())
//                        .p("ReceiveRegion", commonMessage.getHeader().getRegion())
//                        .p("EdgeZone", commonConfig.getZone())
//                        .p("EdgeRegion", commonConfig.getRegion())
//                        .p("ClientIp", clientIp)
//                        .w();
//                return;
//            }
                    TraceHolder.stopWatch(MapBuilder.create(new HashMap<String, String>())
                                    .put(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                                    .put(LogFieldConstants.ACTION, EdgeEvent.Action.ReceiveMsg)
                                    .put(LogFieldConstants.TID, commonMessage.getHeader().getTenantId() + "")
                                    .put(HostStackConstants.CHANNEL_ID, ctx.channel().id().toString())
                                    .put(HostStackConstants.TRACE_ID, commonMessage.getHeader().getTraceId())
                                    .put(HostStackConstants.METH_ID, commonMessage.getHeader().getMethId() + "")
                                    .put(HostStackConstants.TRACE_ID, commonMessage.getHeader().getTraceId())
                                    .put(HostStackConstants.CLIENT_IP, clientIp)
                                    .put(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                                    .put(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                                    .put(HostStackConstants.REGION, EdgeContext.Region)
                                    .put(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                                    .build(),
                            () -> {
                                String idcSid = commonMessage.getHeader().getIdcSid();
                                String relaySid = commonMessage.getHeader().getRelaySid();
                                if (StringUtil.isNotBlank(idcSid) || idcSid.equals(relaySid)) {
                                    // idcId is empty or idcId equals relayId, transfer to agent
                                    edgeClientControllerManager.get(commonMessage.getHeader().getMethId()).ifPresentOrElse(
                                            controller -> controller.handle(ctx, commonMessage),
                                            () -> {
                                                KvLogger.instance(this)
                                                        .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                                                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.ReceiveMsg)
                                                        .p(LogFieldConstants.ERR_MSG, commonMessage.getHeader().getMethId())
                                                        .p(HostStackConstants.METH_ID, ctx.channel().id())
                                                        .p(HostStackConstants.CLIENT_IP, clientIp)
                                                        .e();
                                            });
                                } else if (StringUtil.isNotBlank(idcSid)) {
                                    // transfer to idc
                                    relayControllerManager.get(ProtoMethodId.TransferToIdc.getValue()).handle(ctx, commonMessage);
                                } else {
                                    KvLogger.instance(this)
                                            .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                                            .p(LogFieldConstants.ACTION, EdgeEvent.Action.ReceiveMsg)
                                            .p(LogFieldConstants.ERR_MSG, "UnknownTransferWho")
                                            .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                                            .p(HostStackConstants.TRACE_ID, commonMessage.getHeader().getTraceId())
                                            .p(HostStackConstants.METH_ID, commonMessage.getHeader().getMethId())
                                            .p(HostStackConstants.CLIENT_IP, clientIp)
                                            .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                                            .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                                            .p(HostStackConstants.REGION, EdgeContext.Region)
                                            .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                                            .e();
                                }
                            }
                    );
                } catch (Exception ex) {
                    KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                            .p(LogFieldConstants.ACTION, EdgeEvent.Action.ReceiveMsg)
                            .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                            .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                            .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                            .p(HostStackConstants.REGION, EdgeContext.Region)
                            .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                            .e();
                } finally {
                    ReferenceCountUtil.release(msg);
                }
            });
        } else {
            ReferenceCountUtil.release(msg);
        }
    }
}
