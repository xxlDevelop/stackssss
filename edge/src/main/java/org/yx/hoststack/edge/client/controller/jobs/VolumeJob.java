package org.yx.hoststack.edge.client.controller.jobs;

import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Service;
import org.yx.hoststack.edge.common.JobType;
import org.yx.hoststack.edge.common.exception.UnknownJobException;
import org.yx.hoststack.edge.server.ws.session.Session;
import org.yx.hoststack.edge.server.ws.session.SessionManager;
import org.yx.hoststack.edge.server.ws.session.SessionType;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.hoststack.protocol.ws.agent.jobs.host.UpgradeVolumeJob;
import org.yx.hoststack.protocol.ws.agent.jobs.volume.CreateVolumeJob;
import org.yx.hoststack.protocol.ws.agent.jobs.volume.DeleteVolumeJob;
import org.yx.hoststack.protocol.ws.agent.jobs.volume.MountVolumeJob;
import org.yx.hoststack.protocol.ws.agent.jobs.volume.UnmountVolumeJob;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.JobParams;

import java.util.Map;

@Service(JobType.Volume)
public class VolumeJob extends HostStackJob {

    protected VolumeJob(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    public void doJob(ChannelHandlerContext context, CommonMessageWrapper.Header messageHeader, C2EMessage.C2E_DoJobReq jobReq)
            throws InvalidProtocolBufferException, UnknownJobException {
        switch (jobReq.getJobSubType().toLowerCase()) {
            case "create":
                // TODO if volume diskType is net, need send idc-storageSvc to create net volume
                JobParams.VolumeCreate volumeCreate = JobParams.VolumeCreate.parseFrom(jobReq.getJobParams());
                Session createVolumeTargetSession = getJobTargetSession(volumeCreate.getHostId(), SessionType.Host);
                // send job to host
                volumeCreate.getTargetList().forEach(volumeTarget ->
                        validAgentSession(createVolumeTargetSession, jobReq, volumeTarget.getJobDetailId(), messageHeader.getTraceId(),
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
                                    AgentCommonMessage<?> createVolumeMessage = buildJobMessage(volumeTarget.getJobDetailId(), volumeCreate.getHostId(),
                                            "CreateVolume", messageHeader.getTraceId(), createVolumeJob);
                                    // send job message to agent
                                    sendJobToAgent(agentSession, jobReq, createVolumeMessage, volumeTarget.getJobDetailId(), messageHeader.getTraceId());
                                }));
                break;
            case "delete":
                // TODO if volume diskType is net, need send idc-storageSvc to create net volume
                JobParams.VolumeDelete volumeDelete = JobParams.VolumeDelete.parseFrom(jobReq.getJobParams());
                Map<String, Session> deleteVolumeHostTargetSessions = getJobTargetSessions(Lists.newArrayList(volumeDelete.getHostId()), SessionType.Host);
                Session deleteVolumeTargetSession = deleteVolumeHostTargetSessions.get(volumeDelete.getHostId());
                // send job to host
                volumeDelete.getTargetList().forEach(volumeTarget ->
                        validAgentSession(deleteVolumeTargetSession, jobReq, volumeTarget.getJobDetailId(), messageHeader.getTraceId(),
                                agentSession -> {
                                    // create job data
                                    DeleteVolumeJob deleteVolumeJob = DeleteVolumeJob.builder()
                                            .volumeId(volumeTarget.getVolumeId())
                                            .build();
                                    // create job message
                                    AgentCommonMessage<?> deleteVolumeMessage = buildJobMessage(volumeTarget.getJobDetailId(), volumeDelete.getHostId(),
                                            "DeleteVolume", messageHeader.getTraceId(), deleteVolumeJob);
                                    // send job message to agent
                                    sendJobToAgent(agentSession, jobReq, deleteVolumeMessage, volumeTarget.getJobDetailId(), messageHeader.getTraceId());
                                }));
                break;
            case "mount":
                JobParams.VolumeMount volumeMount = JobParams.VolumeMount.parseFrom(jobReq.getJobParams());
                Map<String, Session> mountVolumeHostTargetSessions = getJobTargetSessions(Lists.newArrayList(volumeMount.getHostId()), SessionType.Host);
                Session mountVolumeHostTargetSession = mountVolumeHostTargetSessions.get(volumeMount.getHostId());
                // send job to host
                volumeMount.getTargetList().forEach(volumeTarget ->
                        validAgentSession(mountVolumeHostTargetSession, jobReq, volumeTarget.getJobDetailId(), messageHeader.getTraceId(),
                                agentSession -> {
                                    // create job data
                                    MountVolumeJob mountVolumeJob = MountVolumeJob.builder()
                                            .volumeId(volumeTarget.getVolumeId())
                                            .baseVolumeId(volumeTarget.getBaseVolumeId())
                                            .cid(volumeTarget.getCid())
                                            .mountType(volumeTarget.getMountType())
                                            .build();
                                    // create job message
                                    AgentCommonMessage<?> mountVolumeMessage = buildJobMessage(volumeTarget.getJobDetailId(), volumeMount.getHostId(),
                                            "MountVolume", messageHeader.getTraceId(), mountVolumeJob);
                                    // send job message to agent
                                    sendJobToAgent(agentSession, jobReq, mountVolumeMessage, volumeTarget.getJobDetailId(), messageHeader.getTraceId());
                                }));
                break;
            case "unmount":
                JobParams.VolumeUnMount volumeUnmount = JobParams.VolumeUnMount.parseFrom(jobReq.getJobParams());
                Map<String, Session> unmountVolumeHostTargetSessions = getJobTargetSessions(Lists.newArrayList(volumeUnmount.getHostId()), SessionType.Host);
                Session unmountVolumeHostTargetSession = unmountVolumeHostTargetSessions.get(volumeUnmount.getHostId());
                // send job to host
                volumeUnmount.getTargetList().forEach(volumeTarget ->
                        validAgentSession(unmountVolumeHostTargetSession, jobReq, volumeTarget.getJobDetailId(), messageHeader.getTraceId(),
                                agentSession -> {
                                    // create job data
                                    UnmountVolumeJob unmountVolumeJob = UnmountVolumeJob.builder()
                                            .volumeId(volumeTarget.getVolumeId())
                                            .cid(volumeTarget.getCid())
                                            .build();
                                    // create job message
                                    AgentCommonMessage<?> unmountVolumeMessage = buildJobMessage(volumeTarget.getJobDetailId(), volumeUnmount.getHostId(),
                                            "UnmountVolume", messageHeader.getTraceId(), unmountVolumeJob);
                                    // send job message to agent
                                    sendJobToAgent(agentSession, jobReq, unmountVolumeMessage, volumeTarget.getJobDetailId(), messageHeader.getTraceId());
                                }));
                break;
            case "upgrade":
                JobParams.VolumeUpgrade volumeUpgrade = JobParams.VolumeUpgrade.parseFrom(jobReq.getJobParams());
                Map<String, Session> upgradeVolumeHostTargetSessions = getJobTargetSessions(Lists.newArrayList(volumeUpgrade.getHostId()), SessionType.Host);
                Session upgradeVolumeHostTargetSession = upgradeVolumeHostTargetSessions.get(volumeUpgrade.getHostId());
                // send job to host
                volumeUpgrade.getTargetList().forEach(volumeTarget ->
                        validAgentSession(upgradeVolumeHostTargetSession, jobReq, volumeTarget.getJobDetailId(), messageHeader.getTraceId(),
                                agentSession -> {
                                    // create job data
                                    UpgradeVolumeJob upgradeVolumeJob = UpgradeVolumeJob.builder()
                                            .originVolumeId(volumeTarget.getOriginVolumeId())
                                            .newVolumeId(volumeTarget.getNewVolumeId())
                                            .keepOrigin(volumeTarget.getKeepOrigin())
                                            .build();
                                    // create job message
                                    AgentCommonMessage<?> unmountVolumeMessage = buildJobMessage(volumeTarget.getJobDetailId(), volumeUpgrade.getHostId(),
                                            "UpgradeVolume", messageHeader.getTraceId(), upgradeVolumeJob);
                                    // send job message to agent
                                    sendJobToAgent(agentSession, jobReq, unmountVolumeMessage, volumeTarget.getJobDetailId(), messageHeader.getTraceId());
                                }));
                break;
            default:
                throw new UnknownJobException();
        }
    }
}
