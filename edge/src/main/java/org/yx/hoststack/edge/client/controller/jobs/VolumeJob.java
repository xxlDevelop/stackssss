package org.yx.hoststack.edge.client.controller.jobs;

import cn.hutool.core.lang.UUID;
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
import org.yx.hoststack.edge.apiservice.storagesvc.req.CreateBaseVolumeReq;
import org.yx.hoststack.edge.apiservice.storagesvc.req.CreateUserVolumeReq;
import org.yx.hoststack.edge.apiservice.storagesvc.req.DeleteVolumeReq;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.common.JobType;
import org.yx.hoststack.edge.common.exception.UnknownJobException;
import org.yx.hoststack.edge.queue.MessageQueues;
import org.yx.hoststack.edge.server.ws.session.Session;
import org.yx.hoststack.edge.server.ws.session.SessionManager;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.hoststack.protocol.ws.agent.jobs.host.UpgradeVolumeJob;
import org.yx.hoststack.protocol.ws.agent.jobs.volume.CreateVolumeJob;
import org.yx.hoststack.protocol.ws.agent.jobs.volume.DeleteVolumeJob;
import org.yx.hoststack.protocol.ws.agent.jobs.volume.MountVolumeJob;
import org.yx.hoststack.protocol.ws.agent.jobs.volume.UnmountVolumeJob;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.JobParams;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.StringUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service(JobType.Volume)
public class VolumeJob extends HostStackJob {
    private final StorageSvcApiService storageSvcApiService;

    protected VolumeJob(JobCacheService jobCacheService,
                        SessionManager sessionManager,
                        StorageSvcApiService storageSvcApiService,
                        MessageQueues messageQueues) {
        super(jobCacheService, sessionManager, messageQueues);
        this.storageSvcApiService = storageSvcApiService;
    }

    @Override
    public void doJob(ChannelHandlerContext context, CommonMessageWrapper.Header messageHeader, C2EMessage.C2E_DoJobReq jobReq)
            throws InvalidProtocolBufferException, UnknownJobException {
        switch (jobReq.getJobSubType().toLowerCase()) {
            case "create":
                JobParams.VolumeCreate volumeCreate = JobParams.VolumeCreate.parseFrom(jobReq.getJobParams());
                if ("local".equalsIgnoreCase(volumeCreate.getDiskType())) {
                    createLocalVolume(messageHeader, volumeCreate, jobReq);
                } else {
                    createNetVolume(messageHeader, volumeCreate, jobReq);
                }
                break;
            case "delete":
                JobParams.VolumeDelete volumeDelete = JobParams.VolumeDelete.parseFrom(jobReq.getJobParams());
                if ("local".equalsIgnoreCase(volumeDelete.getDiskType())) {
                    deleteLocalVolume(messageHeader, volumeDelete, jobReq);
                } else {
                    deleteNetVolume(messageHeader, volumeDelete, jobReq);
                }
                break;
            case "mount":
                JobParams.VolumeMount volumeMount = JobParams.VolumeMount.parseFrom(jobReq.getJobParams());
                Session mountVolumeHostTargetSession = sessionManager.getSession(volumeMount.getHostId());
                // send job to host
                volumeMount.getTargetList().forEach(volumeTarget ->
                        validAgentSession(mountVolumeHostTargetSession, jobReq.getJobId(), volumeTarget.getJobDetailId(), messageHeader.getTraceId(),
                                agentSession -> {
                                    // create job data
                                    MountVolumeJob mountVolumeJob = MountVolumeJob.builder()
                                            .volumeId(volumeTarget.getVolumeId())
                                            .baseVolumeId(volumeTarget.getBaseVolumeId())
                                            .cid(volumeTarget.getCid())
                                            .mountType(volumeTarget.getMountType())
                                            .build();
                                    // create job message
                                    AgentCommonMessage<?> mountVolumeMessage = buildAgentJobMessage(volumeTarget.getJobDetailId(), volumeMount.getHostId(),
                                            "MountVolume", messageHeader.getTraceId(), mountVolumeJob);
                                    // send job message to agent
                                    sendJobToAgent(agentSession, jobReq.getJobId(), jobReq.getJobType(), jobReq.getJobSubType(), mountVolumeMessage, volumeTarget.getJobDetailId(), messageHeader.getTraceId());
                                }));
                break;
            case "unmount":
                JobParams.VolumeUnMount volumeUnmount = JobParams.VolumeUnMount.parseFrom(jobReq.getJobParams());
                Session unmountVolumeHostTargetSession = sessionManager.getSession(volumeUnmount.getHostId());
                // send job to host
                volumeUnmount.getTargetList().forEach(volumeTarget ->
                        validAgentSession(unmountVolumeHostTargetSession, jobReq.getJobId(), volumeTarget.getJobDetailId(), messageHeader.getTraceId(),
                                agentSession -> {
                                    // create job data
                                    UnmountVolumeJob unmountVolumeJob = UnmountVolumeJob.builder()
                                            .volumeId(volumeTarget.getVolumeId())
                                            .cid(volumeTarget.getCid())
                                            .build();
                                    // create job message
                                    AgentCommonMessage<?> unmountVolumeMessage = buildAgentJobMessage(volumeTarget.getJobDetailId(), volumeUnmount.getHostId(),
                                            "UnmountVolume", messageHeader.getTraceId(), unmountVolumeJob);
                                    // send job message to agent
                                    sendJobToAgent(agentSession, jobReq.getJobId(), jobReq.getJobType(), jobReq.getJobSubType(), unmountVolumeMessage, volumeTarget.getJobDetailId(), messageHeader.getTraceId());
                                }));
                break;
            case "upgrade":
                JobParams.VolumeUpgrade volumeUpgrade = JobParams.VolumeUpgrade.parseFrom(jobReq.getJobParams());
                Session upgradeVolumeHostTargetSession = sessionManager.getSession(volumeUpgrade.getHostId());
                // send job to host
                volumeUpgrade.getTargetList().forEach(volumeTarget ->
                        validAgentSession(upgradeVolumeHostTargetSession, jobReq.getJobId(), volumeTarget.getJobDetailId(), messageHeader.getTraceId(),
                                agentSession -> {
                                    // create job data
                                    UpgradeVolumeJob upgradeVolumeJob = UpgradeVolumeJob.builder()
                                            .originVolumeId(volumeTarget.getOriginVolumeId())
                                            .newVolumeId(volumeTarget.getNewVolumeId())
                                            .keepOrigin(volumeTarget.getKeepOrigin())
                                            .build();
                                    // create job message
                                    AgentCommonMessage<?> unmountVolumeMessage = buildAgentJobMessage(volumeTarget.getJobDetailId(), volumeUpgrade.getHostId(),
                                            "UpgradeVolume", messageHeader.getTraceId(), upgradeVolumeJob);
                                    // send job message to agent
                                    sendJobToAgent(agentSession, jobReq.getJobId(), jobReq.getJobType(), jobReq.getJobSubType(), unmountVolumeMessage, volumeTarget.getJobDetailId(), messageHeader.getTraceId());
                                }));
                break;
            default:
                throw new UnknownJobException();
        }
    }

    private void createLocalVolume(CommonMessageWrapper.Header messageHeader, JobParams.VolumeCreate volumeCreate, C2EMessage.C2E_DoJobReq jobReq) {
        Session createVolumeTargetSession = sessionManager.getSession(volumeCreate.getHostId());
        // send job to host
        volumeCreate.getTargetList().forEach(volumeTarget ->
                validAgentSession(createVolumeTargetSession, jobReq.getJobId(), volumeTarget.getJobDetailId(), messageHeader.getTraceId(),
                        agentSession -> {
                            // create job data
                            CreateVolumeJob createVolumeJob = CreateVolumeJob.builder()
                                    .volumeId(volumeTarget.getVolumeId())
                                    .volumeSize(volumeCreate.getVolumeSize())
                                    .diskType(volumeCreate.getDiskType())
                                    .volumeType(volumeCreate.getVolumeType())
                                    .sourceUrl(volumeCreate.getSourceUrl())
                                    .build();
                            // create job message
                            AgentCommonMessage<?> createVolumeMessage = buildAgentJobMessage(volumeTarget.getJobDetailId(), volumeCreate.getHostId(),
                                    "CreateVolume", messageHeader.getTraceId(), createVolumeJob);
                            // send job message to agent
                            sendJobToAgent(agentSession, jobReq.getJobId(), jobReq.getJobType(), jobReq.getJobSubType(), createVolumeMessage, volumeTarget.getJobDetailId(), messageHeader.getTraceId());
                        }));
    }

    private void createNetVolume(CommonMessageWrapper.Header messageHeader, JobParams.VolumeCreate volumeCreate, C2EMessage.C2E_DoJobReq jobReq) {
        KvLogger kvLogger = KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_HTTP_REMOTE)
                .p(LogFieldConstants.TID, messageHeader.getTenantId())
                .p(LogFieldConstants.TRACE_ID, messageHeader.getTraceId())
                .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                .p(HostStackConstants.REGION, EdgeContext.Region)
                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                .p(HostStackConstants.JOB_ID, jobReq.getJobId())
                .p("SnapshotName", volumeCreate.getSnapshotName());

        List<Mono<R<?>>> monoList = Lists.newArrayList();
        List<VolumeOperateR> rList = Lists.newArrayList();

        for (JobParams.VolumeTarget volumeTarget : volumeCreate.getTargetList()) {
            String traceId = UUID.fastUUID().toString(true);
            if ("base".equals(volumeCreate.getVolumeType())) {
                CreateBaseVolumeReq createBaseVolumeReq = new CreateBaseVolumeReq();
                createBaseVolumeReq.setVolumeName(volumeTarget.getVolumeId());
                createBaseVolumeReq.setSize(volumeCreate.getVolumeSize());
                createBaseVolumeReq.setInitMode(StringUtil.isBlank(volumeCreate.getSourceUrl()) ? "default" : "remote");
                createBaseVolumeReq.setInitDownloadUrl(volumeCreate.getSourceUrl());
                createBaseVolumeReq.setPoolName("poolName");
                createBaseVolumeReq.setTid(messageHeader.getTenantId());

                Mono<R<?>> rMono = storageSvcApiService.createBaseVolume(createBaseVolumeReq, traceId)
                        .flatMap(r -> {
                            kvLogger.p(HostStackConstants.JOB_DETAIL_ID, volumeTarget.getJobDetailId())
                                    .p(LogFieldConstants.TRACE_ID, traceId)
                                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.BASE_VOLUME_CREATE)
                                    .p(LogFieldConstants.Code, r.getCode() == R.ok().getCode() ?
                                            R.ok().getCode() : EdgeSysCode.CreateBaseVolumeFailed.getValue())
                                    .p(LogFieldConstants.ERR_MSG, r.getMsg())
                                    .p("VolumeId", volumeTarget.getVolumeId());
                            return getMono(kvLogger, rList, volumeTarget, r);
                        });
                monoList.add(rMono);
            } else {
                CreateUserVolumeReq createUserVolumeReq = new CreateUserVolumeReq();
                createUserVolumeReq.setSnapshotName(volumeCreate.getSnapshotName());
                createUserVolumeReq.setTid(messageHeader.getTenantId());
                createUserVolumeReq.setVolumeName(volumeTarget.getVolumeId());

                Mono<R<?>> rMono = storageSvcApiService.createUserVolume(createUserVolumeReq, traceId)
                        .flatMap(r -> {
                            kvLogger.p(HostStackConstants.JOB_DETAIL_ID, volumeTarget.getJobDetailId())
                                    .p(LogFieldConstants.TRACE_ID, traceId)
                                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.USER_VOLUME_CREATE)
                                    .p(LogFieldConstants.Code, r.getCode() == R.ok().getCode() ?
                                            R.ok().getCode() : EdgeSysCode.CreateUserVolumeFailed.getValue())
                                    .p(LogFieldConstants.ERR_MSG, r.getMsg())
                                    .p("VolumeId", volumeTarget.getVolumeId());
                            return getMono(kvLogger, rList, volumeTarget, r);
                        });
                monoList.add(rMono);
            }
        }
        sendMonoResultToCenter(monoList, rList, messageHeader.getTraceId());
    }

    private void deleteLocalVolume(CommonMessageWrapper.Header messageHeader, JobParams.VolumeDelete volumeDelete, C2EMessage.C2E_DoJobReq jobReq) {
        Session deleteVolumeTargetSession = sessionManager.getSession(volumeDelete.getHostId());
        // send job to host
        volumeDelete.getTargetList().forEach(volumeTarget ->
                validAgentSession(deleteVolumeTargetSession, jobReq.getJobId(), volumeTarget.getJobDetailId(), messageHeader.getTraceId(),
                        agentSession -> {
                            // create job data
                            DeleteVolumeJob deleteVolumeJob = DeleteVolumeJob.builder()
                                    .volumeId(volumeTarget.getVolumeId())
                                    .build();
                            // create job message
                            AgentCommonMessage<?> deleteVolumeMessage = buildAgentJobMessage(volumeTarget.getJobDetailId(), volumeDelete.getHostId(),
                                    "DeleteVolume", messageHeader.getTraceId(), deleteVolumeJob);
                            // send job message to agent
                            sendJobToAgent(agentSession, jobReq.getJobId(), jobReq.getJobType(), jobReq.getJobSubType(), deleteVolumeMessage, volumeTarget.getJobDetailId(), messageHeader.getTraceId());
                        }));
    }

    private void deleteNetVolume(CommonMessageWrapper.Header messageHeader, JobParams.VolumeDelete volumeDelete, C2EMessage.C2E_DoJobReq jobReq) {
        KvLogger kvLogger = KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_HTTP_REMOTE)
                .p(LogFieldConstants.TID, messageHeader.getTenantId())
                .p(LogFieldConstants.TRACE_ID, messageHeader.getTraceId())
                .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                .p(HostStackConstants.REGION, EdgeContext.Region)
                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                .p(HostStackConstants.JOB_ID, jobReq.getJobId());

        List<Mono<R<?>>> monoList = Lists.newArrayList();
        List<VolumeOperateR> rList = Lists.newArrayList();

        for (JobParams.VolumeTarget volumeTarget : volumeDelete.getTargetList()) {
            DeleteVolumeReq deleteVolumeReq = new DeleteVolumeReq();
            deleteVolumeReq.setVolumeName(volumeTarget.getVolumeId());
            deleteVolumeReq.setTid(messageHeader.getTenantId());
            String traceId = UUID.fastUUID().toString(true);

            if ("base".equals(volumeDelete.getVolumeType())) {
                Mono<R<?>> rMono = storageSvcApiService.deleteBaseVolume(deleteVolumeReq, traceId)
                        .flatMap(r -> {
                            kvLogger.p(HostStackConstants.JOB_DETAIL_ID, volumeTarget.getJobDetailId())
                                    .p(LogFieldConstants.TRACE_ID, traceId)
                                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.BASE_VOLUME_DELETE)
                                    .p(LogFieldConstants.Code, r.getCode() == R.ok().getCode() ?
                                            R.ok().getCode() : EdgeSysCode.DeleteBaseVolumeFailed.getValue())
                                    .p(LogFieldConstants.ERR_MSG, r.getMsg())
                                    .p("VolumeId", volumeTarget.getVolumeId());
                            return getMono(kvLogger, rList, volumeTarget, r);
                        });
                monoList.add(rMono);
            } else {
                Mono<R<?>> rMono = storageSvcApiService.deleteUserVolume(deleteVolumeReq, traceId)
                        .flatMap(r -> {
                            kvLogger.p(HostStackConstants.JOB_DETAIL_ID, volumeTarget.getJobDetailId())
                                    .p(LogFieldConstants.TRACE_ID, traceId)
                                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.USER_VOLUME_DELETE)
                                    .p(LogFieldConstants.Code, r.getCode() == R.ok().getCode() ?
                                            R.ok().getCode() : EdgeSysCode.DeleteUserVolumeFailed.getValue())
                                    .p(LogFieldConstants.ERR_MSG, r.getMsg());
                            return getMono(kvLogger, rList, volumeTarget, r);
                        });
                monoList.add(rMono);
            }
        }
        sendMonoResultToCenter(monoList, rList, messageHeader.getTraceId());
    }

    @Getter
    @Setter
    @Builder
    private static class VolumeOperateR {
        private String volumeId;
        private String jobDetailId;
        private int code;
        private String msg;
    }

    private Mono<? extends R<?>> getMono(KvLogger kvLogger, List<VolumeOperateR> rList,
                                         JobParams.VolumeTarget volumeTarget, R<?> r) {
        if (r.getCode() == R.ok().getCode()) {
            kvLogger.i();
        } else {
            kvLogger.w();
        }
        rList.add(VolumeOperateR.builder()
                .volumeId(volumeTarget.getVolumeId())
                .jobDetailId(volumeTarget.getJobDetailId())
                .code(r.getCode())
                .msg(r.getCode() == R.ok().getCode() ? "" : r.getMsg())
                .build());
        return Mono.just(r);
    }

    private void sendMonoResultToCenter(List<Mono<R<?>>> monoList, List<VolumeOperateR> rList, String traceId) {
        Flux.fromIterable(monoList).flatMap(rMono -> rMono).doOnComplete(() -> {
            List<AgentCommonMessage<?>> jobReports = Lists.newArrayList();
            rList.forEach(r -> jobReports.add(
                    AgentCommonMessage.builder()
                            .jobId(r.getJobDetailId())
                            .status(r.getCode() == R.ok().getCode() ? "success" : "fail")
                            .progress(100)
                            .code(r.getCode())
                            .msg(r.getMsg())
                            .traceId(traceId)
                            .build())
            );
            sendJobToCenter(jobReports);
        }).subscribe();
    }
}
