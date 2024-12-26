package org.yx.hoststack.edge.server.ws.controller;

import cn.hutool.core.lang.UUID;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yx.hoststack.edge.client.EdgeClientConnector;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.queue.MessageQueues;
import org.yx.hoststack.edge.server.ws.controller.manager.EdgeServerControllerManager;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.hoststack.protocol.ws.agent.common.AgentMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.nio.file.Files;

@Service
@RequiredArgsConstructor
public class JobNotifyController extends BaseController {
    {
        EdgeServerControllerManager.add(AgentMethodId.JobNotify, this::jobNotify);
    }

    private final MessageQueues messageQueues;

    private void jobNotify(ChannelHandlerContext context, AgentCommonMessage<?> agentReport) {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.Job)
                .p(LogFieldConstants.ACTION, EdgeEvent.Action.AgentJobNotify)
                .p("JobId", agentReport.getJobId())
                .p("JobStatus", agentReport.getStatus())
                .p("JobProgress", agentReport.getProgress())
                .p(LogFieldConstants.Code, agentReport.getCode())
                .p(LogFieldConstants.ERR_MSG, agentReport.getMsg())
                .i();
        EdgeClientConnector.getInstance().sendJobNotifyReport(agentReport, UUID.fastUUID().toString(), null,
                () -> messageQueues.getJobNotifyNotSendQueue().add(agentReport));
    }
}
