package org.yx.hoststack.edge.client.controller.jobs;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import org.yx.hoststack.edge.client.EdgeClientConnector;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.common.exception.UnknownJobException;
import org.yx.hoststack.edge.queue.MessageQueues;
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
import org.yx.lib.utils.util.R;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class HostStackJob {
    protected final SessionManager sessionManager;

    private final MessageQueues messageQueues;

    protected HostStackJob(SessionManager sessionManager, MessageQueues messageQueues) {
        this.sessionManager = sessionManager;
        this.messageQueues = messageQueues;
    }

    public abstract void doJob(ChannelHandlerContext context, CommonMessageWrapper.Header messageHeader, C2EMessage.C2E_DoJobReq jobReq)
            throws InvalidProtocolBufferException, UnknownJobException;

    protected <T> AgentCommonMessage<T> buildAgentJobMessage(String jobId, String hostId, String jobMethod, String traceId, T data) {
        return AgentCommonMessage.<T>builder()
                .type(MessageType.REQUEST)
                .hostId(hostId)
                .method(jobMethod)
                .traceId(traceId)
                .jobId(jobId)
                .data(data)
                .build();
    }

    protected void sendJobToAgent(Session agentSession, String jobId, AgentCommonMessage<?> agentJobMessage,
                                  String jobDetailId, String jobTraceId) {
        agentSession.sendMsg(ProtoMethodId.DoJob.getValue(), agentJobMessage,
                null,
                () -> EdgeClientConnector.getInstance().sendJobFailedToUpstream(jobId,
                        jobDetailId, EdgeSysCode.SendAgentFailByChannelNotActive.getValue(), EdgeSysCode.SendAgentFailByChannelNotActive.getMsg(),
                        jobTraceId));
    }

    protected void validAgentSession(Session agentSession, String jobId,
                                     String jobDetailId, String jobTraceId, Consumer<Session> sessionConsumer) {
        Optional.ofNullable(agentSession).ifPresentOrElse(sessionConsumer,
                () -> EdgeClientConnector.getInstance().sendJobFailedToUpstream(jobId,
                        jobDetailId, EdgeSysCode.NotFoundAgentSession.getValue(), EdgeSysCode.NotFoundAgentSession.getMsg(), jobTraceId));
    }

    protected void sendJobToCenter(String jobId, String traceId, R<?> distributeR) {
        AgentCommonMessage<?> jobReport = AgentCommonMessage.builder()
                .jobId(jobId)
                .status(distributeR.getCode() == R.ok().getCode() ? "processing" : "fail")
                .progress(distributeR.getCode() == R.ok().getCode() ? 20 : 100)
                .code(distributeR.getCode())
                .msg(distributeR.getCode() == R.ok().getCode() ? "" : distributeR.getMsg())
                .traceId(traceId)
                .build();
        sendJobToCenter(Lists.newArrayList(jobReport));
    }

    protected void sendJobToCenter(List<AgentCommonMessage<?>> jobReports) {
        messageQueues.getJobNotifyToCenterQueue().addAll(jobReports);
    }
}
