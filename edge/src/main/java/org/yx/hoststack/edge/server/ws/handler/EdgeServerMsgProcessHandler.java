package org.yx.hoststack.edge.server.ws.handler;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.common.syscode.EdgeSysCode;
import org.yx.hoststack.common.utils.NetUtils;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.server.RunMode;
import org.yx.hoststack.edge.server.ws.controller.manager.EdgeServerControllerManager;
import org.yx.hoststack.edge.transfer.manager.RelayControllerManager;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
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
//            Object channelClientId = "";
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
                        .i();
//                channelManager.closeChannel(ctx);
                return;
            }
            if (msg instanceof TextWebSocketFrame socketFrame) {
                // receive agent json text
                processAgentMsg(socketFrame, ctx, clientIp);
            } else if (EdgeContext.RunMode.equalsIgnoreCase(RunMode.RELAY) && msg instanceof BinaryWebSocketFrame binaryWebSocketFrame) {
                // receive idc protobuf transfer to center
                transferToCenterMsg(binaryWebSocketFrame, ctx);
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
                        .w();
            }
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.ProcessMsg)
                    .p(LogFieldConstants.Code, EdgeSysCode.Exception.getValue())
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                    .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                    .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                    .p(HostStackConstants.REGION, EdgeContext.Region)
                    .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                    .e(e);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void processAgentMsg(TextWebSocketFrame socketFrame, ChannelHandlerContext ctx, String clientIp) {
        if (socketFrame.isFinalFragment()) {
            String fullFrameText = socketFrame.text();
            AgentCommonMessage<?> agentCommonMessage = JSON.parseObject(fullFrameText, AgentCommonMessage.class);
            KvLogger kvLogger = KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.ReceiveMsg)
                    .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                    .p(HostStackConstants.TRACE_ID, agentCommonMessage.getTraceId())
                    .p(HostStackConstants.METH_ID, agentCommonMessage.getMethod())
                    .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                    .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                    .p(HostStackConstants.REGION, EdgeContext.Region)
                    .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                    .p(HostStackConstants.AGENT_ID, agentCommonMessage.getHostId())
                    .p(HostStackConstants.CLIENT_IP, clientIp);

            if (agentCommonMessage.getType().equalsIgnoreCase(MessageType.RESPONSE)) {
                kvLogger.p(LogFieldConstants.ERR_MSG, "MessageType Error")
                        .p("MessageType", agentCommonMessage.getType())
                        .w();
                return;
            }
            kvLogger.i();
            kvLogger.p(LogFieldConstants.ReqData, fullFrameText).d();
            edgeServerControllerManager.get(agentCommonMessage.getMethod()).ifPresentOrElse(
                    controller -> controller.handle(ctx, agentCommonMessage),
                    () -> kvLogger.p(LogFieldConstants.ERR_MSG, "UnknownMsgId").e());
        }
    }

    private void transferToCenterMsg(BinaryWebSocketFrame msg, ChannelHandlerContext ctx) throws InvalidProtocolBufferException {
        // transfer to center
        ByteBuf byteBuf = msg.content();
        byte[] contentBytes = ByteBufUtil.getBytes(byteBuf);
        CommonMessageWrapper.CommonMessage idcMessage = CommonMessageWrapper.CommonMessage.parseFrom(contentBytes);
        relayControllerManager.get(ProtoMethodId.TransferToCenter.getValue()).handle(ctx, idcMessage);
    }
}

