package org.yx.hoststack.edge.forwarding;

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.common.syscode.EdgeSysCode;
import org.yx.hoststack.edge.client.EdgeClient;
import org.yx.hoststack.edge.client.EdgeClientConnector;
import org.yx.hoststack.edge.common.ChannelType;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.common.KvMappingChannelContextTempData;
import org.yx.hoststack.edge.forwarding.manager.RelayControllerManager;
import org.yx.hoststack.edge.forwarding.manager.ForwardingNode;
import org.yx.hoststack.edge.forwarding.manager.ForwardingNodeMgr;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.E2CMessage;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.SpringContextHolder;

@Service
@RequiredArgsConstructor
public class RelayForwardingController {
    {
        RelayControllerManager.add(ProtoMethodId.ForwardingToCenter, this::forwardingToCenter);
        RelayControllerManager.add(ProtoMethodId.ForwardingToIdc, this::forwardingToIdc);
    }

    @Value("${sessionTimeout.idc:10}")
    private int idcSessionTimeout;

    private final KvMappingChannelContextTempData kvMappingChannelContextTempData;
    private final ForwardingNodeMgr forwardingNodeMgr;

    private void forwardingToCenter(ChannelHandlerContext context, CommonMessageWrapper.CommonMessage message) {
        KvLogger kvLogger = KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.FORWARDING_PROTOCOL)
                .p(HostStackConstants.METH_ID, message.getHeader().getMethId())
                .p(HostStackConstants.TRACE_ID, message.getHeader().getTraceId())
                .p(HostStackConstants.CHANNEL_ID, context.channel().id())
                .p(HostStackConstants.IDC_SID, message.getHeader().getIdcSid())
                .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                .p(HostStackConstants.REGION, message.getHeader().getRegion());

        if (message.getHeader().getLinkSide() == CommonMessageWrapper.ENUM_LINK_SIDE.EDGE_TO_CENTER_VALUE) {
            // relay transfer center, if message is idc register, hold this message, prepare create idc session to use
            if (message.getHeader().getMethId() == ProtoMethodId.EdgeRegister.getValue()) {
                kvMappingChannelContextTempData.put(message.getHeader().getIdcSid(), context);
            }
            if (message.getHeader().getMethId() == ProtoMethodId.Ping.getValue()) {
                forwardingNodeMgr.get(message.getHeader().getIdcSid()).ifPresentOrElse(
                        forwardingNode -> {
                            if (message.getHeader().getMethId() == ProtoMethodId.Ping.getValue()) {
                                forwardingNode.hb();
                            }
                        },
                        () -> {
                            kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.FORWARDING_TO_CENTER_FAIL)
                                    .p(LogFieldConstants.ERR_MSG, "Not found idc session, close")
                                    .w();
                            context.close();
                        });
            }
            EdgeClientConnector.getInstance().sendMsg(buildRelaySendMessage(message),
                    () -> {
                        kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.FORWARDING_TO_CENTER)
                                .p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId())
                                .p(LogFieldConstants.Code, 0);
                        if (message.getHeader().getMethId() == ProtoMethodId.Ping.getValue() ||
                                message.getHeader().getMethId() == ProtoMethodId.Pong.getValue()) {
                            kvLogger.d();
                        } else {
                            kvLogger.i();
                        }
                    },
                    () -> {
                        kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.FORWARDING_TO_CENTER_FAIL)
                                .p(LogFieldConstants.Code, EdgeSysCode.UpstreamServiceNotAvailable.getValue())
                                .p(LogFieldConstants.ERR_MSG, EdgeSysCode.UpstreamServiceNotAvailable.getMsg())
                                .w();
                        sendRelayTransferFailMsg(message, EdgeSysCode.UpstreamServiceNotAvailable.getValue(),
                                EdgeSysCode.UpstreamServiceNotAvailable.getMsg(), context, CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE);
                    });
        }
    }

    private void forwardingToIdc(ChannelHandlerContext context, CommonMessageWrapper.CommonMessage message) {
        KvLogger kvLogger = KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.FORWARDING_PROTOCOL)
                .p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId())
                .p(HostStackConstants.REGION, message.getHeader().getRegion())
                .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                .p(HostStackConstants.IDC_SID, message.getHeader().getIdcSid())
                .p(HostStackConstants.METH_ID, message.getHeader().getMethId());

        if (message.getHeader().getLinkSide() == CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE) {
            if (message.getHeader().getMethId() == ProtoMethodId.EdgeRegister.getValue()) {
                try {
                    C2EMessage.C2E_EdgeRegisterResp registerResp = C2EMessage.C2E_EdgeRegisterResp.parseFrom(message.getBody().getPayload());
                    ChannelHandlerContext idcChannelContext = kvMappingChannelContextTempData.get(message.getHeader().getIdcSid());
                    if (idcChannelContext == null) {
                        kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.FORWARDING_TO_IDC)
                                .p(LogFieldConstants.Code, EdgeSysCode.DownloadStreamServiceNotAvailable.getValue())
                                .p(LogFieldConstants.ERR_MSG, EdgeSysCode.DownloadStreamServiceNotAvailable.getMsg())
                                .w();
                        sendRelayTransferFailMsg(message, EdgeSysCode.DownloadStreamServiceNotAvailable.getValue(),
                                EdgeSysCode.DownloadStreamServiceNotAvailable.getMsg(), context, CommonMessageWrapper.ENUM_LINK_SIDE.EDGE_TO_CENTER_VALUE);
                        return;
                    }
                    if (message.getBody().getCode() == R.ok().getCode()) {
                        idcChannelContext.channel().attr(AttributeKey.valueOf(HostStackConstants.CHANNEL_TYPE)).set(ChannelType.IDC);
                        idcChannelContext.channel().attr(AttributeKey.valueOf(HostStackConstants.IDC_SID)).set(message.getHeader().getIdcSid());
                        ForwardingNode forwardingNode = new ForwardingNode(message.getHeader().getIdcSid(), idcChannelContext, idcSessionTimeout, registerResp.getHbInterval());
                        // create idc transfer node
                        forwardingNodeMgr.addForwardingNode(forwardingNode);
                        kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.CREATE_FORWARDING_NODE)
                                .p("NodeId", message.getHeader().getIdcSid())
                                .i();
                        forwardingNode.sendMsg(buildRelaySendMessage(message));
                    } else {
                        kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.FORWARDING_TO_IDC)
                                .p(LogFieldConstants.Code, message.getBody().getCode())
                                .p(LogFieldConstants.ERR_MSG, message.getBody().getMsg())
                                .e();

                        byte[] protobufMessage = message.toByteArray();
                        ByteBuf byteBuf = Unpooled.wrappedBuffer(protobufMessage);
                        idcChannelContext.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
                    }
                    kvMappingChannelContextTempData.remove(message.getHeader().getIdcSid());
                } catch (InvalidProtocolBufferException e) {
                    kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.FORWARDING_TO_IDC)
                            .e(e);
                }
            } else {
                forwardingNodeMgr.get(message.getHeader().getIdcSid()).ifPresentOrElse(
                        forwardingNode -> {
                            forwardingNode.sendMsg(buildRelaySendMessage(message));
                        },
                        () -> {
                            kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.FORWARDING_TO_IDC)
                                    .p(LogFieldConstants.ERR_MSG, "Not found idc session, close")
                                    .p("TargetIdc", message.getHeader().getIdcSid())
                                    .w();
                            context.close();
                        });
            }
        }
    }

    private CommonMessageWrapper.CommonMessage buildRelaySendMessage(CommonMessageWrapper.CommonMessage idcMessage) {
        return CommonMessageWrapper.CommonMessage.newBuilder()
                .setHeader(CommonMessageWrapper.CommonMessage.newBuilder().getHeaderBuilder()
                        .setLinkSide(idcMessage.getHeader().getLinkSide())
                        .setProtocolVer(idcMessage.getHeader().getProtocolVer())
                        .setZone(idcMessage.getHeader().getZone())
                        .setRegion(idcMessage.getHeader().getRegion())
                        .setRelaySid(EdgeContext.RelayServiceId)
                        .setIdcSid(idcMessage.getHeader().getIdcSid())
                        .setMethId(idcMessage.getHeader().getMethId())
                        .setTraceId(idcMessage.getHeader().getTraceId()))
                .setBody(idcMessage.getBody())
                .build();
    }

    private void sendRelayTransferFailMsg(CommonMessageWrapper.CommonMessage message,
                                          int code, String msg, ChannelHandlerContext context, int linkSide) {
        CommonMessageWrapper.CommonMessage failMessage = CommonMessageWrapper.CommonMessage.newBuilder()
                .setHeader(CommonMessageWrapper.Header.newBuilder()
                        .setLinkSide(linkSide)
                        .setTraceId(message.getHeader().getTraceId())
                        .setProtocolVer(message.getHeader().getProtocolVer())
                        .setZone(message.getHeader().getZone())
                        .setRegion(message.getHeader().getRegion())
                        .setIdcSid(message.getHeader().getIdcSid())
                        .setRelaySid(EdgeContext.RelayServiceId)
                        .setTenantId(message.getHeader().getTenantId())
                        .setMethId(ProtoMethodId.ForwardingFailed.getValue())
                        .setTimestamp(System.currentTimeMillis())
                        .build())
                .setBody(CommonMessageWrapper.Body.newBuilder()
                        .setCode(code)
                        .setMsg(msg)
                        .setPayload(E2CMessage.ForwardFailedNotify.newBuilder()
                                .setMethId(message.getHeader().getMethId())
                                .build()
                                .toByteString())
                        .build())
                .build();
        byte[] protobufMessage = failMessage.toByteArray();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(protobufMessage);
        context.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
    }
}
