package org.yx.hoststack.edge.client.controller.jobs;

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Service;
import org.yx.hoststack.edge.common.JobType;
import org.yx.hoststack.edge.common.exception.UnknownJobException;
import org.yx.hoststack.edge.queue.MessageQueues;
import org.yx.hoststack.edge.server.ws.session.SessionManager;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.JobParams;

@Service(JobType.Module)
public class ModuleJob extends HostStackJob {
    protected ModuleJob(JobCacheService jobCacheService, SessionManager sessionManager, MessageQueues messageQueues) {
        super(jobCacheService, sessionManager, messageQueues);
    }
    @Override
    public void doJob(ChannelHandlerContext context, CommonMessageWrapper.Header messageHeader, C2EMessage.C2E_DoJobReq jobReq)
            throws InvalidProtocolBufferException, UnknownJobException {
        switch (jobReq.getJobSubType().toLowerCase()) {
            case "create":
                JobParams.ModuleCreate moduleCreate = JobParams.ModuleCreate.parseFrom(jobReq.getJobParams());
                // TODO call storageSvc to transfer net image download to local net
                break;
            case "upgrade":
                JobParams.ModuleUpgrade moduleUpgrade = JobParams.ModuleUpgrade.parseFrom(jobReq.getJobParams());
                break;
            default:
                throw new UnknownJobException();
        }
    }
}
