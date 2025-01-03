package org.yx.hoststack.edge.client.controller.jobs;

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.yx.hoststack.edge.common.JobType;
import org.yx.hoststack.edge.common.exception.UnknownJobException;
import org.yx.hoststack.edge.server.ws.session.SessionManager;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.JobParams;

@Service(JobType.Image)
public class ImageJob extends HostStackJob {
    protected ImageJob(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    public void doJob(ChannelHandlerContext context, CommonMessageWrapper.Header messageHeader, C2EMessage.C2E_DoJobReq jobReq)
            throws InvalidProtocolBufferException, UnknownJobException {
        switch (jobReq.getJobSubType().toLowerCase()) {
            case "create":
                JobParams.ImageCreate imageCreate = JobParams.ImageCreate.parseFrom(jobReq.getJobParams());
                // TODO call storageSvc to transfer net image download to local net
                break;
            case "delete":
                JobParams.ImageDelete imageDelete = JobParams.ImageDelete.parseFrom(jobReq.getJobParams());
                // TODO call storageSvc to delete image
                break;
            default:
                throw new UnknownJobException();
        }
    }
}
