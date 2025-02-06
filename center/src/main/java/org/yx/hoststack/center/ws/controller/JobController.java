package org.yx.hoststack.center.ws.controller;

import com.google.protobuf.ByteString;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.queue.MessageQueues;
import org.yx.hoststack.center.queue.message.JobReportMessage;
import org.yx.hoststack.center.ws.controller.manager.CenterControllerManager;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.common.QueueMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.E2CMessage;
import org.yx.hoststack.protocol.ws.server.JobResult;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;


/**
 * Process Center Basic Message
 */
@Service
@RequiredArgsConstructor
public class JobController {
    private final MessageQueues messageQueues;

    {
        CenterControllerManager.add(ProtoMethodId.JobReport, this::jobReport);
    }

    /**
     * Edge Register Center Success Result
     *
     * @param ctx     ChannelHandlerContext
     * @param message CommonMessage
     */
    private void jobReport(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message) {
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
                .p(LogFieldConstants.ACTION, CenterEvent.Action.JOB_NOTIFY)
                .p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId())
                .p(LogFieldConstants.TID, message.getHeader().getTenantId())
                .p(HostStackConstants.IDC_SID, message.getHeader().getIdcSid())
                .p(HostStackConstants.RELAY_SID, message.getHeader().getRelaySid())
                .p(HostStackConstants.REGION, message.getHeader().getRegion());
        try {
            ByteString payload = message.getBody().getPayload();
            E2CMessage.E2C_JobReportReq jobReportReq = E2CMessage.E2C_JobReportReq.parseFrom(payload);
            for (E2CMessage.JobReportItem jobReportItem : jobReportReq.getItemsList()) {
                JobResult.JobTargetResult jobTargetResult = JobResult.JobTargetResult.parseFrom(jobReportItem.getJobResult());
                for (JobResult.TargetResult targetResult : jobTargetResult.getTargetResultList()) {
                    messageQueues.getJobReportQueue().add(new QueueMessage<>(jobReportItem.getJobType(),
                            JobReportMessage.builder()
                                    .jobId(jobReportItem.getJobId())
                                    .jobDetailId(targetResult.getJobDetailId())
                                    .traceId(jobReportItem.getTraceId())
                                    .status(targetResult.getStatus())
                                    .code(targetResult.getCode())
                                    .msg(targetResult.getMsg())
                                    .progress(targetResult.getProgress())
                                    .output(targetResult.getOutput())
                                    .jobType(jobReportItem.getJobType())
                                    .jobSubType(jobReportItem.getJobSubType())
                                    .build())
                    );
                }
            }
        } catch (Exception ex) {
            kvLogger.p(LogFieldConstants.ERR_MSG, ex.getMessage())
                    .e(ex);
        }
    }
}
