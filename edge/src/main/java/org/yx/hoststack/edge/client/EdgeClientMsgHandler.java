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
import org.yx.hoststack.common.TraceHolder;
import org.yx.hoststack.common.syscode.EdgeSysCode;
import org.yx.hoststack.common.utils.NetUtils;
import org.yx.hoststack.edge.client.controller.manager.EdgeClientControllerManager;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.config.EdgeCommonConfig;
import org.yx.hoststack.edge.forwarding.manager.RelayControllerManager;
import org.yx.hoststack.edge.server.RunMode;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
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
    private final EdgeCommonConfig edgeCommonConfig;

    public EdgeClientMsgHandler(@Qualifier("edgeExecutor") Executor executorService,
                                EdgeClientControllerManager edgeClientControllerManager,
                                RelayControllerManager relayControllerManager,
                                EdgeCommonConfig edgeCommonConfig) {
        this.executorService = executorService;
        this.edgeClientControllerManager = edgeClientControllerManager;
        this.relayControllerManager = relayControllerManager;
        this.edgeCommonConfig = edgeCommonConfig;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_CLIENT)
                .p(LogFieldConstants.EVENT, EdgeEvent.Action.CLOSE_BY_SERVER)
                .p(LogFieldConstants.ERR_MSG, "Maybe upstream not ready or close by server")
                .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                .p(HostStackConstants.REGION, EdgeContext.Region)
                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                .i();
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
                    KvLogger kvLogger = KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_CLIENT)
                            .p(LogFieldConstants.ACTION, EdgeEvent.Action.RECEIVE_MSG)
                            .p(LogFieldConstants.TRACE_ID, commonMessage.getHeader().getTraceId())
                            .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                            .p(HostStackConstants.TRACE_ID, commonMessage.getHeader().getTraceId())
                            .p(HostStackConstants.IDC_SID, commonMessage.getHeader().getIdcSid())
                            .p(HostStackConstants.RELAY_SID, commonMessage.getHeader().getRelaySid())
                            .p(HostStackConstants.REGION, EdgeContext.Region)
                            .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                            .p(HostStackConstants.CLIENT_IP, clientIp);

                    if (commonMessage.getHeader().getLinkSide() != CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE) {
                        kvLogger.p(LogFieldConstants.Code, EdgeSysCode.LinkSideError.getValue())
                                .p(LogFieldConstants.ERR_MSG, EdgeSysCode.LinkSideError.getMsg())
                                .p("HeaderLinkSide", commonMessage.getHeader().getLinkSide())
                                .w();
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
                                    .put(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_CLIENT)
                                    .put(LogFieldConstants.ACTION, EdgeEvent.Action.STATIS)
                                    .put(LogFieldConstants.TID, commonMessage.getHeader().getTenantId() + "")
                                    .put(HostStackConstants.CHANNEL_ID, ctx.channel().id().toString())
                                    .put(HostStackConstants.TRACE_ID, commonMessage.getHeader().getTraceId())
                                    .put(HostStackConstants.METH_ID, commonMessage.getHeader().getMethId() + "")
                                    .put(HostStackConstants.CLIENT_IP, clientIp)
                                    .put(HostStackConstants.IDC_SID, commonMessage.getHeader().getIdcSid())
                                    .put(HostStackConstants.RELAY_SID, commonMessage.getHeader().getRelaySid())
                                    .put(HostStackConstants.REGION, EdgeContext.Region)
                                    .put(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                                    .build(),
                            () -> {
                                String idcSid = commonMessage.getHeader().getIdcSid();
                                String relaySid = commonMessage.getHeader().getRelaySid();
                                if (edgeCommonConfig.getRunMode().equals(RunMode.IDC) && !EdgeContext.IdcServiceId.equals(idcSid)) {
                                    kvLogger.p(LogFieldConstants.Code, EdgeSysCode.IdcSidMismatched.getValue())
                                            .p(LogFieldConstants.ERR_MSG, EdgeSysCode.IdcSidMismatched.getMsg())
                                            .p("HeaderIdcSid", idcSid)
                                            .p("SelfIdcSid", EdgeContext.IdcServiceId)
                                            .w();
                                    return;
                                }
                                if (edgeCommonConfig.getRunMode().equals(RunMode.RELAY) && !EdgeContext.RelayServiceId.equals(relaySid)) {
                                    kvLogger.p(LogFieldConstants.Code, EdgeSysCode.RelaySidMismatched.getValue())
                                            .p(LogFieldConstants.ERR_MSG, EdgeSysCode.RelaySidMismatched.getMsg())
                                            .p("HeaderRelaySid", relaySid)
                                            .p("SelfRelaySid", EdgeContext.RelayServiceId)
                                            .w();
                                    return;
                                }
                                if ((EdgeContext.RunMode.equalsIgnoreCase(RunMode.IDC) && StringUtil.isNotBlank(idcSid)) ||
                                        EdgeContext.RunMode.equalsIgnoreCase(RunMode.RELAY) && StringUtil.isBlank(idcSid) && StringUtil.isNoneBlank(relaySid)) {
                                    edgeClientControllerManager.get(commonMessage.getHeader().getMethId()).ifPresentOrElse(
                                            controller -> controller.handle(ctx, commonMessage),
                                            () -> KvLogger.instance(this)
                                                    .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_CLIENT)
                                                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.RECEIVE_MSG)
                                                    .p(LogFieldConstants.ERR_MSG, "Unknown MethId")
                                                    .p(HostStackConstants.IDC_SID, commonMessage.getHeader().getIdcSid())
                                                    .p(HostStackConstants.RELAY_SID, commonMessage.getHeader().getRelaySid())
                                                    .p(HostStackConstants.METH_ID, commonMessage.getHeader().getMethId())
                                                    .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                                                    .p(HostStackConstants.CLIENT_IP, clientIp)
                                                    .w());
                                } else if (EdgeContext.RunMode.equalsIgnoreCase(RunMode.RELAY) && StringUtil.isNotBlank(idcSid)) {
                                    // transfer to idc
                                    relayControllerManager.get(ProtoMethodId.ForwardingToIdc.getValue()).handle(ctx, commonMessage);
                                } else {
                                    kvLogger.p(LogFieldConstants.ERR_MSG, "UnknownTransferWho")
                                            .w();
                                }
                            }
                    );
                } catch (Exception ex) {
                    KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_CLIENT)
                            .p(LogFieldConstants.ACTION, EdgeEvent.Action.RECEIVE_MSG)
                            .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                            .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                            .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                            .p(HostStackConstants.REGION, EdgeContext.Region)
                            .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                            .e(ex);
                } finally {
                    ReferenceCountUtil.release(msg);
                }
            });
        } else {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.fireExceptionCaught(cause);
        ctx.close();
    }
}
