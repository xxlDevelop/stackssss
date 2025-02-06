package org.yx.hoststack.edge.server.ws.controller;

import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.queue.MessageQueues;
import org.yx.hoststack.edge.server.ws.controller.manager.EdgeServerControllerManager;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.hoststack.protocol.ws.agent.common.AgentMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

@Service
@RequiredArgsConstructor
public class JobNotifyController extends BaseController {
    {
        EdgeServerControllerManager.add(AgentMethodId.JobNotify, this::jobNotify);
    }

    private final MessageQueues messageQueues;

    private void jobNotify(ChannelHandlerContext context, AgentCommonMessage<?> agentReport) {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.JOB)
                .p(LogFieldConstants.ACTION, EdgeEvent.Action.AGENT_JOB_NOTIFY)
                .p("JobId", agentReport.getJobId())
                .p("JobStatus", agentReport.getStatus())
                .p("JobProgress", agentReport.getProgress())
                .p(LogFieldConstants.Code, agentReport.getCode())
                .p(LogFieldConstants.ERR_MSG, agentReport.getMsg())
                .i();
        messageQueues.getJobNotifyToCenterQueue().add(agentReport);
    }
}
