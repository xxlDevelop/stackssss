package org.yx.hoststack.edge.client.controller.jobs;

import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Service;
import org.yx.hoststack.edge.common.JobType;
import org.yx.hoststack.edge.common.exception.NotFoundSessionException;
import org.yx.hoststack.edge.common.exception.UnknownJobException;
import org.yx.hoststack.edge.server.ws.session.Session;
import org.yx.hoststack.edge.server.ws.session.SessionManager;
import org.yx.hoststack.edge.server.ws.session.SessionType;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.hoststack.protocol.ws.agent.jobs.container.ContainerUpdateProfileJob;
import org.yx.hoststack.protocol.ws.agent.jobs.container.CreateContainerJob;
import org.yx.hoststack.protocol.ws.agent.jobs.container.CtrlContainerJob;
import org.yx.hoststack.protocol.ws.agent.jobs.container.UpgradeContainerJob;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.JobParams;

@Service(JobType.Container)
public class ContainerJob extends HostStackJob {

    protected ContainerJob(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    public void doJob(ChannelHandlerContext context, CommonMessageWrapper.Header messageHeader, C2EMessage.C2E_DoJobReq jobReq)
            throws InvalidProtocolBufferException, UnknownJobException, NotFoundSessionException {
        switch (jobReq.getJobSubType().toLowerCase()) {
            case "create":
                JobParams.ContainerCreate containerCreate = JobParams.ContainerCreate.parseFrom(jobReq.getJobParams());
                Session createContainerHostTargetSession = getJobTargetSession(containerCreate.getHostId(), SessionType.Host);
                // send job to host
                containerCreate.getTargetList().forEach(target ->
                        validAgentSession(createContainerHostTargetSession, jobReq, target.getJobDetailId(), messageHeader.getTraceId(),
                                agentSession -> {
                                    // create job data
                                    CreateContainerJob createContainerJob = CreateContainerJob.builder()
                                            .vmType(containerCreate.getVmType())
                                            .image(CreateContainerJob.Image.builder()
                                                    .id(containerCreate.getImage().getId())
                                                    .url(containerCreate.getImage().getUrl())
                                                    .ver(containerCreate.getImage().getVer())
                                                    .md5(containerCreate.getImage().getMd5())
                                                    .user(containerCreate.getImage().getUser())
                                                    .password(containerCreate.getImage().getPwd())
                                                    .sourceType(containerCreate.getImage().getSourceType())
                                                    .build())
                                            .profileTemplate(containerCreate.getProfileTemplate())
                                            .containerList(Lists.newArrayList(CreateContainerJob.Container.builder()
                                                    .profile(target.getProfile())
                                                    .cid(target.getCid())
                                                    .build()))
                                            .build();
                                    // create job message
                                    AgentCommonMessage<?> createContainerMessage = buildJobMessage(target.getJobDetailId(),
                                            containerCreate.getHostId(), "CreateVM", messageHeader.getTraceId(), createContainerJob);
                                    // send job message to agent
                                    sendJobToAgent(agentSession, jobReq, createContainerMessage,
                                            target.getJobDetailId(), messageHeader.getTraceId());
                                }));
                break;
            case "upgrade":
                JobParams.ContainerUpgrade containerUpgrade = JobParams.ContainerUpgrade.parseFrom(jobReq.getJobParams());
                Session upgradeContainerHostTargetSession = getJobTargetSession(containerUpgrade.getHostId(), SessionType.Host);
                // send job to host
                containerUpgrade.getTargetList().forEach(target ->
                        validAgentSession(upgradeContainerHostTargetSession, jobReq, target.getJobDetailId(), messageHeader.getTraceId(),
                                agentSession -> {
                                    // create job data
                                    UpgradeContainerJob upgradeContainerJob = UpgradeContainerJob.builder()
                                            .image(UpgradeContainerJob.Image.builder()
                                                    .id(containerUpgrade.getImage().getId())
                                                    .url(containerUpgrade.getImage().getUrl())
                                                    .ver(containerUpgrade.getImage().getVer())
                                                    .md5(containerUpgrade.getImage().getMd5())
                                                    .user(containerUpgrade.getImage().getUser())
                                                    .password(containerUpgrade.getImage().getPwd())
                                                    .sourceType(containerUpgrade.getImage().getSourceType())
                                                    .build())
                                            .cids(Lists.newArrayList(target.getCid()))
                                            .build();
                                    // create job message
                                    AgentCommonMessage<?> upgradeContainerMessage = buildJobMessage(target.getJobDetailId(),
                                            containerUpgrade.getHostId(), "UpgradeImage", messageHeader.getTraceId(), upgradeContainerJob);
                                    // send job message to agent
                                    sendJobToAgent(agentSession, jobReq, upgradeContainerMessage,
                                            target.getJobDetailId(), messageHeader.getTraceId());
                                }));
                break;
            case "updateprofile":
                JobParams.ContainerUpdateProfile containerUpdateProfile = JobParams.ContainerUpdateProfile.parseFrom(jobReq.getJobParams());
                Session updateProfileContainerHostTargetSession = getJobTargetSession(containerUpdateProfile.getHostId(), SessionType.Host);
                // send job to host
                containerUpdateProfile.getTargetList().forEach(target ->
                        validAgentSession(updateProfileContainerHostTargetSession, jobReq, target.getJobDetailId(), messageHeader.getTraceId(),
                                agentSession -> {
                                    // create job data
                                    ContainerUpdateProfileJob containerUpdateProfileJob = ContainerUpdateProfileJob.builder()
                                            .containerList(Lists.newArrayList(ContainerUpdateProfileJob.Container.builder()
                                                    .profile(target.getProfile())
                                                    .build()))
                                            .build();
                                    // create job message
                                    AgentCommonMessage<?> upgradeProfileMessage = buildJobMessage(target.getJobDetailId(),
                                            containerUpdateProfile.getHostId(), "ModifyVM", messageHeader.getTraceId(), containerUpdateProfileJob);
                                    // send job message to agent
                                    sendJobToAgent(agentSession, jobReq, upgradeProfileMessage,
                                            target.getJobDetailId(), messageHeader.getTraceId());
                                }));
                break;
            case "ctrl":
                JobParams.ContainerCtrl containerCtrl = JobParams.ContainerCtrl.parseFrom(jobReq.getJobParams());
                Session ctrlContainerHostTargetSession = getJobTargetSession(containerCtrl.getHostId(), SessionType.Host);
                // send job to host
                containerCtrl.getTargetList().forEach(target ->
                        validAgentSession(ctrlContainerHostTargetSession, jobReq, target.getJobDetailId(), messageHeader.getTraceId(),
                                agentSession -> {
                                    // create job data
                                    CtrlContainerJob ctrlContainerJob = CtrlContainerJob.builder()
                                            .oper(containerCtrl.getCtrl())
                                            .cids(Lists.newArrayList(target.getCid()))
                                            .build();
                                    // create job message
                                    AgentCommonMessage<?> ctrlContainerMessage = buildJobMessage(target.getJobDetailId(),
                                            containerCtrl.getHostId(), "ControlVM", messageHeader.getTraceId(), ctrlContainerJob);
                                    // send job message to agent
                                    sendJobToAgent(agentSession, jobReq, ctrlContainerMessage,
                                            target.getJobDetailId(), messageHeader.getTraceId());
                                }));
                break;
            case "execcmd":
                //TODO
                break;
            default:
                throw new UnknownJobException();
        }
    }
}
