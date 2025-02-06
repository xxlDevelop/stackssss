package org.yx.hoststack.edge.client.controller.jobs;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.net.url.UrlBuilder;
import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.common.syscode.EdgeSysCode;
import org.yx.hoststack.edge.apiservice.storagesvc.StorageSvcApiService;
import org.yx.hoststack.edge.apiservice.storagesvc.req.DistributeFileReq;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.common.JobType;
import org.yx.hoststack.edge.common.exception.UnknownJobException;
import org.yx.hoststack.edge.config.EdgeCommonConfig;
import org.yx.hoststack.edge.queue.MessageQueues;
import org.yx.hoststack.edge.server.ws.session.SessionManager;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.JobParams;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service(JobType.Image)
public class ImageJob extends HostStackJob {
    private final StorageSvcApiService storageSvcApiService;
    private final EdgeCommonConfig edgeCommonConfig;

    protected ImageJob(JobCacheService jobCacheService,
                       SessionManager sessionManager,
                       StorageSvcApiService storageSvcApiService,
                       EdgeCommonConfig edgeCommonConfig,
                       MessageQueues messageQueues) {
        super(jobCacheService, sessionManager, messageQueues);
        this.storageSvcApiService = storageSvcApiService;
        this.edgeCommonConfig = edgeCommonConfig;
    }

    @Override
    public void doJob(ChannelHandlerContext context, CommonMessageWrapper.Header messageHeader, C2EMessage.C2E_DoJobReq jobReq)
            throws InvalidProtocolBufferException, UnknownJobException {
        KvLogger kvLogger = KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_HTTP_REMOTE)
                .p(LogFieldConstants.TID, messageHeader.getTenantId())
                .p(LogFieldConstants.TRACE_ID, messageHeader.getTraceId())
                .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                .p(HostStackConstants.REGION, EdgeContext.Region)
                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                .p(HostStackConstants.JOB_ID, jobReq.getJobId());

        switch (jobReq.getJobSubType().toLowerCase()) {
            case "create":
                JobParams.ImageCreate imageCreate = JobParams.ImageCreate.parseFrom(jobReq.getJobParams());
                DistributeFileReq distributeFileReq = new DistributeFileReq();
                distributeFileReq.setObjectKey(imageCreate.getImageId());
                distributeFileReq.setDownloadUrl(imageCreate.getDownloadUrl());
                distributeFileReq.setMd5(imageCreate.getMd5());
                distributeFileReq.setTid(messageHeader.getTenantId());
                distributeFileReq.setBucket(imageCreate.getBucket());
                distributeFileReq.setJobID(imageCreate.getJobDetailId());
                String callbackUrl = UrlBuilder.of("http", edgeCommonConfig.getLocalIp(), edgeCommonConfig.getServerPort(),
                        "/host-stack-edge/file/distribute/notify",
                        "imageId=" + imageCreate.getImageId() + "&imageVer=" + imageCreate.getImageVer(),
                        "", StandardCharsets.UTF_8).build();
                distributeFileReq.setCallbackUrl(callbackUrl);
                String createTraceId = UUID.fastUUID().toString(true);
                storageSvcApiService.distributeFile(distributeFileReq, createTraceId).subscribe(r -> {
                    kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.IMAGE_DISTRIBUTE)
                            .p(LogFieldConstants.ERR_CODE, r.getCode())
                            .p(LogFieldConstants.ERR_MSG, r.getMsg())
                            .p(LogFieldConstants.TRACE_ID, createTraceId)
                            .p("ImageId", imageCreate.getImageId())
                            .p("Bucket", imageCreate.getBucket())
                            .p("CallbackUrl", callbackUrl)
                            .i();
                    jobCacheService.createJob(JobCacheData.builder()
                            .jobId(jobReq.getJobId())
                            .jobDetailId(imageCreate.getJobDetailId())
                            .jobType(JobType.Image)
                            .jobSubType("create")
                            .build());
                    sendJobToCenter(imageCreate.getJobDetailId(), messageHeader.getTraceId(), r);
                });
                break;
            case "delete":
                List<Mono<R<?>>> monoList = Lists.newArrayList();
                List<ImageDeleteR> rList = Lists.newArrayList();

                JobParams.ImageDelete imageDelete = JobParams.ImageDelete.parseFrom(jobReq.getJobParams());
                for (JobParams.ImageDeleteTarget target : imageDelete.getTargetList()) {
                    String deleteTraceId = UUID.fastUUID().toString(true);
                    jobCacheService.createJob(JobCacheData.builder()
                            .jobId(jobReq.getJobId())
                            .jobDetailId(target.getJobDetailId())
                            .jobType(JobType.Image)
                            .jobSubType("delete")
                            .build());
                    Mono<R<?>> rMono = storageSvcApiService.deleteObject(messageHeader.getTenantId(), target.getBucket(), target.getImageId(), deleteTraceId)
                            .flatMap(r -> {
                                kvLogger.p(LogFieldConstants.TRACE_ID, deleteTraceId)
                                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.IMAGE_DELETE)
                                        .p(LogFieldConstants.ERR_CODE, r.getCode() == R.ok().getCode() ?
                                                R.ok().getCode() : EdgeSysCode.DeleteImageFailed.getValue())
                                        .p(LogFieldConstants.ERR_MSG, r.getMsg());
                                if (r.getCode() == R.ok().getCode()) {
                                    kvLogger.i();
                                } else {
                                    kvLogger.w();
                                }
                                rList.add(ImageDeleteR.builder()
                                        .imageId(target.getImageId())
                                        .jobDetailId(target.getJobDetailId())
                                        .code(r.getCode())
                                        .msg(r.getCode() == R.ok().getCode() ? "" : r.getMsg())
                                        .build());
                                return Mono.just(r);
                            });
                    monoList.add(rMono);
                }
                Flux.fromIterable(monoList).flatMap(rMono -> rMono).doOnComplete(() -> {
                    List<AgentCommonMessage<?>> jobReports = Lists.newArrayList();
                    rList.forEach(deleteR -> {
                        jobReports.add(AgentCommonMessage.builder()
                                .jobId(deleteR.getJobDetailId())
                                .status(deleteR.getCode() == R.ok().getCode() ? "success" : "fail")
                                .progress(100)
                                .code(deleteR.getCode())
                                .msg(deleteR.getMsg())
                                .traceId(messageHeader.getTraceId())
                                .build());
                        sendJobToCenter(jobReports);
                    });
                }).subscribe();
                break;
            default:
                throw new UnknownJobException();
        }
    }

    @Getter
    @Setter
    @Builder
    private static class ImageDeleteR {
        private String imageId;
        private String jobDetailId;
        private int code;
        private String msg;
    }
}
