package org.yx.hoststack.edge.client.controller;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.edge.client.EdgeClientConnector;
import org.yx.hoststack.edge.client.controller.manager.EdgeClientControllerManager;
import org.yx.hoststack.edge.common.ChannelType;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.common.KvMappingChannelContextTempData;
import org.yx.hoststack.edge.server.ws.session.Session;
import org.yx.hoststack.edge.server.ws.session.SessionAttrKeys;
import org.yx.hoststack.edge.server.ws.session.SessionManager;
import org.yx.hoststack.edge.server.ws.session.SessionType;
import org.yx.hoststack.protocol.ws.agent.common.AgentMethodId;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.hoststack.protocol.ws.agent.common.MessageType;
import org.yx.hoststack.protocol.ws.agent.resp.HostInitializeResp;
import org.yx.hoststack.common.syscode.EdgeSysCode;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;

@Service
@RequiredArgsConstructor
public class HostInitializeRespController {
    {
        EdgeClientControllerManager.add(ProtoMethodId.HostInitialize, this::hostInitializeResult);
    }

    @Value("${sessionTimeout.agent:120}")
    private int sessionTimeout;
    private final SessionManager sessionManager;
    private final KvMappingChannelContextTempData kvMappingChannelContextTempData;

    /**
     * Host Initialize From Center Result
     * @param ctx   ChannelHandlerContext
     * @param message CommonMessage
     */
    private void hostInitializeResult(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message) {
        KvLogger kvLogger = KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.BUSINESS)
                .p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId())
                .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                .p(HostStackConstants.METH_ID, message.getHeader().getMethId());
        C2EMessage.C2E_HostInitializeResp hostInitializeResult;
        try {
            hostInitializeResult = C2EMessage.C2E_HostInitializeResp.parseFrom(message.getBody().getPayload());
        } catch (InvalidProtocolBufferException e) {
            kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.HOST_INITIALIZE_FAILED)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId())
                    .p(HostStackConstants.METH_ID, message.getHeader().getMethId())
                    .e(e);
            EdgeClientConnector.getInstance().sendResultToUpstream(message.getHeader().getMethId(),
                    EdgeSysCode.PortoParseException.getValue(), EdgeSysCode.PortoParseException.getMsg(), ByteString.EMPTY, message.getHeader().getTraceId());
            return;
        }
        ChannelHandlerContext hostChannelContext = kvMappingChannelContextTempData.get(hostInitializeResult.getDevSn());
        if (message.getBody().getCode() == R.ok().getCode()) {
            kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.HOST_INITIALIZE_SUCCESSFUL)
                    .p("HostId", hostInitializeResult.getHostId())
                    .p("DevSn", hostInitializeResult.getDevSn())
                    .i();
            // host initialize success, create host/container agent session
            if (hostChannelContext != null) {
                hostChannelContext.channel().attr(AttributeKey.valueOf(HostStackConstants.CHANNEL_TYPE)).set(ChannelType.AGENT);
                hostChannelContext.channel().attr(AttributeKey.valueOf(HostStackConstants.AGENT_ID)).set(hostInitializeResult.getHostId());
                Session session = sessionManager.createSession(hostChannelContext, hostInitializeResult.getHostId(), SessionType.from(hostInitializeResult.getAgentType()), sessionTimeout);
                session.setAttr(SessionAttrKeys.AgentId, hostInitializeResult.getHostId());
                session.setAttr(SessionAttrKeys.AgentType, hostInitializeResult.getAgentType());
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_SERVER)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.CREATE_HOST_SESSION)
                        .p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId())
                        .p(HostStackConstants.SESSION_ID, session.getSessionId())
                        .p(HostStackConstants.CHANNEL_ID, hostChannelContext.channel().id())
                        .p(HostStackConstants.AGENT_ID, hostInitializeResult.getHostId())
                        .p(HostStackConstants.HOST_TYPE, hostInitializeResult.getAgentType())
                        .i();
                // send agent host initialize result
                session.sendMsg(message.getHeader().getMethId(), AgentCommonMessage.<HostInitializeResp>builder()
                                .type(MessageType.RESPONSE)
                                .method(AgentMethodId.InitializeMachine.getValue())
                                .traceId(message.getHeader().getTraceId())
                                .code(0)
                                .data(HostInitializeResp.builder().hostId(hostInitializeResult.getHostId()).build())
                                .build(), null,
                        () -> EdgeClientConnector.getInstance().sendResultToUpstream(message.getHeader().getMethId(),
                                EdgeSysCode.SendAgentFailByChannelNotActive.getValue(), EdgeSysCode.SendAgentFailByChannelNotActive.getMsg(),
                                ByteString.EMPTY, message.getHeader().getTraceId()));
            } else {
                KvLogger.instance(this).p(LogFieldConstants.EVENT, EdgeEvent.BUSINESS)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.HOST_INITIALIZE_FAILED)
                        .p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId())
                        .p(LogFieldConstants.ERR_MSG, "Can`t find channelContext from HostInitializeTempDataMap")
                        .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                        .p(HostStackConstants.AGENT_ID, hostInitializeResult.getHostId())
                        .e();
            }
        } else {
            KvLogger.instance(this).p(LogFieldConstants.EVENT, EdgeEvent.BUSINESS)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.HOST_INITIALIZE_FAILED)
                    .p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId())
                    .p(LogFieldConstants.Code, message.getBody().getCode())
                    .p(LogFieldConstants.ERR_MSG, message.getBody().getMsg())
                    .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                    .w();
            if (hostChannelContext != null) {
                hostChannelContext.writeAndFlush(new TextWebSocketFrame(
                        JSON.toJSONString(AgentCommonMessage.<HostInitializeResp>builder()
                                .type(MessageType.RESPONSE)
                                .method(AgentMethodId.InitializeMachine.getValue())
                                .traceId(message.getHeader().getTraceId())
                                .code(message.getBody().getCode())
                                .msg(message.getBody().getMsg())
                                .data(HostInitializeResp.builder().hostId(hostInitializeResult.getHostId()).build())
                                .build())
                ));
            }
        }
        kvMappingChannelContextTempData.remove(hostInitializeResult.getDevSn());
    }
}
