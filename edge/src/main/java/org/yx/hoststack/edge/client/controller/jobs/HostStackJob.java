package org.yx.hoststack.edge.client.controller.jobs;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import org.yx.hoststack.edge.client.EdgeClientConnector;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.common.UnknownJobException;
import org.yx.hoststack.edge.server.ws.session.Session;
import org.yx.hoststack.edge.server.ws.session.SessionAttrKeys;
import org.yx.hoststack.edge.server.ws.session.SessionManager;
import org.yx.hoststack.edge.server.ws.session.SessionType;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.hoststack.protocol.ws.agent.common.MessageType;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.common.syscode.EdgeSysCode;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class HostStackJob {
    protected final SessionManager sessionManager;

    protected HostStackJob(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public abstract void doJob(ChannelHandlerContext context, CommonMessageWrapper.Header messageHeader, C2EMessage.C2E_DoJobReq jobReq) throws InvalidProtocolBufferException, UnknownJobException;

    public Map<String, Session> getJobTargetSessions(List<String> agentIds, SessionType sessionType) {
        List<Session> sessions = sessionManager.getSessions(sessionType);
        Map<String, Session> matchAgentIdSessions = Maps.newConcurrentMap();
        List<String> matchAgentIdList = Lists.newArrayList();
        sessions.forEach(session -> {
            if (agentIds.stream().anyMatch(hostId ->
                    session.getAttr(SessionAttrKeys.AgentId) != null && session.getAttr(SessionAttrKeys.AgentId).equals(hostId))) {
                matchAgentIdSessions.put(session.getAttr(SessionAttrKeys.AgentId).toString(), session);
                matchAgentIdList.add(session.getAttr(SessionAttrKeys.AgentId).toString());
            }
        });
        if (matchAgentIdSessions.size() != agentIds.size()) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.Job)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.JobFindTargetSession)
                    .p(LogFieldConstants.ERR_MSG, "Session count mismatch, maybe in another servers")
                    .p("SourceTargetCount", agentIds.size())
                    .p("MismatchCount", matchAgentIdSessions.size())
                    .p("MissAgentIds", agentIds.removeAll(matchAgentIdList))
                    .w();
        }
        return matchAgentIdSessions;
    }

    public Session getJobTargetSession(String agentId, SessionType sessionType) {
        Map<String, Session> targetSessions = getJobTargetSessions(Lists.newArrayList(agentId), sessionType);
        if (!targetSessions.containsKey(agentId)) {
            return null;
        }
        return targetSessions.get(agentId);
    }

    protected <T> AgentCommonMessage<T> buildJobMessage(String jobId, String hostId, String jobMethod, String traceId, T data) {
        return AgentCommonMessage.<T>builder()
                .type(MessageType.REQUEST)
                .hostId(hostId)
                .method(jobMethod)
                .traceId(traceId)
                .jobId(jobId)
                .data(data)
                .build();
    }

    protected void sendJobToAgent(Session agentSession, C2EMessage.C2E_DoJobReq centerJobReq, AgentCommonMessage<?> agentJobMessage,
                                  String jobDetailId, String jobTraceId) {
        agentSession.sendMsg(ProtoMethodId.DoJob.getValue(), agentJobMessage,
                null,
                () -> EdgeClientConnector.getInstance().sendJobFailedToUpstream(centerJobReq.getJobId(),
                        jobDetailId, EdgeSysCode.SendAgentFailByChannelNotActive.getValue(), EdgeSysCode.SendAgentFailByChannelNotActive.getMsg(),
                        jobTraceId));
    }

    protected void validAgentSession(Session agentSession, C2EMessage.C2E_DoJobReq centerJobReq,
                                     String jobDetailId, String jobTraceId, Consumer<Session> sessionConsumer) {
        Optional.ofNullable(agentSession).ifPresentOrElse(sessionConsumer,
                () -> EdgeClientConnector.getInstance().sendJobFailedToUpstream(centerJobReq.getJobId(),
                        jobDetailId, EdgeSysCode.NotFoundAgentSession.getValue(), EdgeSysCode.NotFoundAgentSession.getMsg(), jobTraceId));
    }
}
