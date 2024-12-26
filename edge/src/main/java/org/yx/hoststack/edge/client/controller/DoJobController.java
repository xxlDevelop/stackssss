package org.yx.hoststack.edge.client.controller;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.edge.client.EdgeClientConnector;
import org.yx.hoststack.edge.client.controller.jobs.JobFactory;
import org.yx.hoststack.edge.client.controller.manager.EdgeClientControllerManager;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.common.UnknownJobException;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.common.syscode.EdgeSysCode;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

@Service
@RequiredArgsConstructor
public class DoJobController {
    {
        EdgeClientControllerManager.add(ProtoMethodId.DoJob, this::doJob);
    }

    private final JobFactory jobFactory;

    private void doJob(ChannelHandlerContext context, CommonMessageWrapper.CommonMessage message) {
        C2EMessage.C2E_DoJobReq doJobReq;
        try {
            doJobReq = C2EMessage.C2E_DoJobReq.parseFrom(message.getBody().getPayload());
        } catch (InvalidProtocolBufferException e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.Job)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.ProcessJob)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId())
                    .p(HostStackConstants.METH_ID, message.getHeader().getMethId())
                    .e(e);
            EdgeClientConnector.getInstance().sendResultToUpstream(message.getHeader().getMethId(),
                    EdgeSysCode.PortoParseException.getValue(), EdgeSysCode.PortoParseException.getMsg(), ByteString.EMPTY, message.getHeader().getTraceId());
            return;
        }
        try {
            // do job business
            jobFactory.get(doJobReq.getJobType().toLowerCase()).doJob(context, message.getHeader(), doJobReq);
        } catch (UnknownJobException unknownJobException) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.Job)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.ProcessJob)
                    .p(LogFieldConstants.ERR_MSG, "UnknownJob")
                    .p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId())
                    .p(HostStackConstants.METH_ID, ProtoMethodId.DoJob)
                    .p(HostStackConstants.JOB_ID, doJobReq.getJobId())
                    .p(HostStackConstants.JOB_TYPE, doJobReq.getJobType())
                    .p(HostStackConstants.JOB_SUB_TYPE, doJobReq.getJobSubType())
                    .e();
            EdgeClientConnector.getInstance().sendJobFailedToUpstream(doJobReq.getJobId(),
                    "", EdgeSysCode.UnknownJob.getValue(), EdgeSysCode.UnknownJob.getMsg(), message.getHeader().getTraceId());
        } catch (Exception ex) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.Job)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.ProcessJob)
                    .p(LogFieldConstants.ERR_MSG, ex.getMessage())
                    .p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId())
                    .p(HostStackConstants.METH_ID, ProtoMethodId.DoJob)
                    .p(HostStackConstants.JOB_ID, doJobReq.getJobId())
                    .p(HostStackConstants.JOB_TYPE, doJobReq.getJobType())
                    .p(HostStackConstants.JOB_SUB_TYPE, doJobReq.getJobSubType())
                    .e(ex);
            EdgeClientConnector.getInstance().sendJobFailedToUpstream(doJobReq.getJobId(),
                    "", EdgeSysCode.DoJobException.getValue(), EdgeSysCode.DoJobException.getMsg(), message.getHeader().getTraceId());
        }
    }
}
