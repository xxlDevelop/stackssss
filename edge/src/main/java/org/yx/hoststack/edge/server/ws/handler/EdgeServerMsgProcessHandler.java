package org.yx.hoststack.edge.server.ws.handler;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.common.utils.NetUtils;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.server.RunMode;
import org.yx.hoststack.edge.server.ws.controller.manager.EdgeServerControllerManager;
import org.yx.hoststack.edge.transfer.manager.RelayControllerManager;
import org.yx.hoststack.protocol.ws.agent.common.CommonMessage;
import org.yx.hoststack.protocol.ws.agent.common.MessageType;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

@Slf4j
@Service
@RequiredArgsConstructor
public class EdgeServerMsgProcessHandler implements ChannelHandler {
    private final EdgeServerControllerManager edgeServerControllerManager;
    private final RelayControllerManager relayControllerManager;

    @Override
    public void doHandle(ChannelHandlerContext ctx, Object msg) {
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
            String clientIp = NetUtils.parseChannelRemoteAddr(ctx.channel());
            if (msg instanceof CloseWebSocketFrame) {
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.HostActiveClose)
                        .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                        .p(HostStackConstants.CLIENT_IP, clientIp)
                        .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                        .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                        .p(HostStackConstants.REGION, EdgeContext.Region)
                        .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                        // TODO 补全数据
                        .i();
//                channelManager.closeChannel(ctx);
                return;
            }
            // receive agent json text
            if (msg instanceof TextWebSocketFrame) {
                WebSocketFrame socketFrame = (WebSocketFrame) msg;
                if (socketFrame.isFinalFragment()) {
                    String fullFrameText = ((TextWebSocketFrame) socketFrame).text();
                    CommonMessage<?> commonMessage = JSON.parseObject(fullFrameText, CommonMessage.class);
                    if (commonMessage.getType().equalsIgnoreCase(MessageType.RESPONSE)) {
                        KvLogger.instance(this)
                                .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                                .p(LogFieldConstants.ACTION, EdgeEvent.Action.ReceiveMsg)
                                .p(LogFieldConstants.ERR_MSG, "MessageType Error")
                                .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                                .p(HostStackConstants.TRACE_ID, commonMessage.getTraceId())
                                .p(HostStackConstants.METH_ID, commonMessage.getMethod())
                                .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                                .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                                .p(HostStackConstants.REGION, EdgeContext.Region)
                                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                                .p(HostStackConstants.AGENT_ID, commonMessage.getHostId())
                                .p(HostStackConstants.CLIENT_IP, clientIp)
                                .p("MessageType", commonMessage.getType())
                                .e();
                        return;
                    }
                    KvLogger kvLogger = KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                            .p(LogFieldConstants.ACTION, EdgeEvent.Action.ReceiveMsg)
                            .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                            .p(HostStackConstants.TRACE_ID, commonMessage.getTraceId())
                            .p(HostStackConstants.METH_ID, commonMessage.getMethod())
                            .p(HostStackConstants.AGENT_ID, commonMessage.getHostId())
                            .p(HostStackConstants.CLIENT_IP, clientIp)
                            .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                            .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                            .p(HostStackConstants.REGION, EdgeContext.Region)
                            .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode);
                    kvLogger.i();
                    kvLogger.p(LogFieldConstants.ReqData, fullFrameText).d();
                    edgeServerControllerManager.get(commonMessage.getMethod()).ifPresentOrElse(
                            controller -> controller.handle(ctx, commonMessage),
                            () -> {
                                KvLogger.instance(this)
                                        .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.ReceiveMsg)
                                        .p(LogFieldConstants.ERR_MSG, commonMessage.getMethod())
                                        .p(HostStackConstants.METH_ID, ctx.channel().id())
                                        .p(HostStackConstants.CLIENT_IP, clientIp)
                                        .e();
                            });
                }
            }
            // receive transfer protobuf
            if (EdgeContext.RunMode.equalsIgnoreCase(RunMode.RELAY) && msg instanceof BinaryWebSocketFrame) {
                // transfer to center
                ByteBuf byteBuf = ((BinaryWebSocketFrame) msg).content();
                byte[] contentBytes = ByteBufUtil.getBytes(byteBuf);
                CommonMessageWrapper.CommonMessage idcMessage = CommonMessageWrapper.CommonMessage.parseFrom(contentBytes);
                relayControllerManager.get(ProtoMethodId.TransferToCenter.getValue()).handle(ctx, idcMessage);
            } else {
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.ReceiveMsg)
                        .p(LogFieldConstants.ERR_MSG, "UnknownWhoReceive")
                        .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                        .p(HostStackConstants.CLIENT_IP, clientIp)
                        .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                        .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                        .p(HostStackConstants.REGION, EdgeContext.Region)
                        .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                        .e();
            }
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.ProcessMsg)
                    .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                    .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                    .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                    .p(HostStackConstants.REGION, EdgeContext.Region)
                    .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                    .e(e);
//            channelManager.closeChannel(ctx);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}

