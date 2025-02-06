package org.yx.hoststack.edge.server.ws.controller;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.queue.MessageQueues;
import org.yx.hoststack.edge.queue.message.HostHeartMessage;
import org.yx.hoststack.edge.server.ws.controller.manager.EdgeServerControllerManager;
import org.yx.hoststack.edge.server.ws.session.Session;
import org.yx.hoststack.edge.server.ws.session.SessionAttrKeys;
import org.yx.hoststack.edge.server.ws.session.SessionManager;
import org.yx.hoststack.protocol.ws.agent.common.AgentMethodId;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.hoststack.protocol.ws.agent.common.MessageType;
import org.yx.hoststack.protocol.ws.agent.req.HostHeartbeatReq;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

@Service
@RequiredArgsConstructor
public class HeartbeatController extends BaseController {
    {
        EdgeServerControllerManager.add(AgentMethodId.HeartBeat, this::hostHeartBeat);
    }

    private final SessionManager sessionManager;
    private final MessageQueues messageQueues;


    private void hostHeartBeat(ChannelHandlerContext context, AgentCommonMessage<?> agentCommonMessage) {
        String channelId = context.channel().id().toString();
        Object clientIp = getAttr(context.channel(), HostStackConstants.CLIENT_IP);
        Session session = sessionManager.getSession(agentCommonMessage.getHostId());
        if (session != null) {
            session.tick();
            HostHeartbeatReq hostHeartbeatReq = ((JSONObject) agentCommonMessage.getData()).toJavaObject(HostHeartbeatReq.class);
            messageQueues.getHostHbQueue().add(HostHeartMessage.builder()
                    .hostId(session.getAttr(SessionAttrKeys.AgentId).toString())
                    .agentType(session.getAttr(SessionAttrKeys.AgentType).toString())
                    .hostHeartbeatReq(hostHeartbeatReq)
                    .build());
            session.sendMsg(ProtoMethodId.HostHeartbeat.getValue(),
                    AgentCommonMessage.builder()
                            .type(MessageType.RESPONSE)
                            .method(agentCommonMessage.getMethod())
                            .hostId(agentCommonMessage.getHostId())
                            .type(agentCommonMessage.getTraceId())
                            .build(), null, null);
        } else {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.BUSINESS)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.NOT_FOUND_HOST_SESSION)
                    .p(HostStackConstants.CHANNEL_ID, channelId)
                    .p(HostStackConstants.TRACE_ID, agentCommonMessage.getTraceId())
                    .p(HostStackConstants.CLIENT_IP, clientIp)
                    .i();
        }
    }
}
