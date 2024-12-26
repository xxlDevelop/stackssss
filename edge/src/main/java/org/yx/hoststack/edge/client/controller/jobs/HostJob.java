package org.yx.hoststack.edge.client.controller.jobs;

import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Service;
import org.yx.hoststack.edge.common.JobType;
import org.yx.hoststack.edge.common.NotFoundSessionException;
import org.yx.hoststack.edge.common.UnknownJobException;
import org.yx.hoststack.edge.server.ws.session.Session;
import org.yx.hoststack.edge.server.ws.session.SessionManager;
import org.yx.hoststack.edge.server.ws.session.SessionType;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.hoststack.protocol.ws.agent.jobs.host.HostExecCmdJob;
import org.yx.hoststack.protocol.ws.agent.jobs.host.HostUpdateConfigJob;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.JobParams;

import java.util.List;
import java.util.Map;

@Service(JobType.Host)
public class HostJob extends HostStackJob {

    public HostJob(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    public void doJob(ChannelHandlerContext context, CommonMessageWrapper.Header messageHeader, C2EMessage.C2E_DoJobReq jobReq)
            throws InvalidProtocolBufferException, UnknownJobException, NotFoundSessionException {
        switch (jobReq.getJobSubType().toLowerCase()) {
            case "reset":
                JobParams.HostReset hostReset = JobParams.HostReset.parseFrom(jobReq.getJobParams());
                List<String> resetHostTargetIds = hostTargetToHostIdList(hostReset.getTargetList());
                Map<String, Session> resetHostTargetSessions = getJobTargetSessions(resetHostTargetIds, SessionType.Host);
                // send job to host
                hostReset.getTargetList().forEach(hostTarget -> {
                    Session targetSession = resetHostTargetSessions.get(hostTarget.getHostId());
                    validAgentSession(targetSession, jobReq, hostTarget.getJobDetailId(), messageHeader.getTraceId(),
                            agentSession -> {
                                // create job message
                                AgentCommonMessage<?> jobMessage = buildJobMessage(hostTarget.getJobDetailId(), hostTarget.getHostId(),
                                        "ResetHost", messageHeader.getTraceId(), null);
                                // send job message to agent
                                sendJobToAgent(agentSession, jobReq, jobMessage, hostTarget.getJobDetailId(), messageHeader.getTraceId());
                            });
                });
                break;
            case "updateConfig":
                JobParams.HostUpdateConfig hostUpdateConfig = JobParams.HostUpdateConfig.parseFrom(jobReq.getJobParams());
                List<String> updateHostConfigTargetIds = hostTargetToHostIdList(hostUpdateConfig.getTargetList());
                Map<String, Session> updateHostConfigTargetSessions = getJobTargetSessions(updateHostConfigTargetIds, SessionType.Host);
                // send job to host
                hostUpdateConfig.getTargetList().forEach(hostTarget -> {
                    Session targetSession = updateHostConfigTargetSessions.get(hostTarget.getHostId());
                    validAgentSession(targetSession, jobReq, hostTarget.getJobDetailId(), messageHeader.getTraceId(),
                            agentSession -> {
                                // create job data
                                List<HostUpdateConfigJob.Config> configDetails = Lists.newArrayList();
                                for (JobParams.HostToUpdateConfigDetail configDetail : hostUpdateConfig.getConfigList()) {
                                    configDetails.add(HostUpdateConfigJob.Config.builder()
                                            .type(configDetail.getType())
                                            .context(configDetail.getContextMap())
                                            .build());
                                }
                                // create job message
                                AgentCommonMessage<?> jobMessage = buildJobMessage(hostTarget.getJobDetailId(), hostTarget.getHostId(),
                                        "UpdateConfig", messageHeader.getTraceId(), configDetails);
                                // send job message to agent
                                sendJobToAgent(agentSession, jobReq, jobMessage, hostTarget.getJobDetailId(), messageHeader.getTraceId());
                            });
                });
                break;
            case "execCmd":
                JobParams.HostExecCmd hostExecCmd = JobParams.HostExecCmd.parseFrom(jobReq.getJobParams());
                List<String> execCmdHostTargetIds = hostTargetToHostIdList(hostExecCmd.getTargetList());
                Map<String, Session> execCmdHostTargetIdSessions = getJobTargetSessions(execCmdHostTargetIds, SessionType.Host);
                // send job to host
                hostExecCmd.getTargetList().forEach(hostTarget -> {
                    Session targetSession = execCmdHostTargetIdSessions.get(hostTarget.getHostId());
                    validAgentSession(targetSession, jobReq, hostTarget.getJobDetailId(), messageHeader.getTraceId(),
                            agentSession -> {
                                // create job data
                                HostExecCmdJob hostExecCmdJob = HostExecCmdJob.builder()
                                        .script(hostExecCmd.getScript())
                                        .build();
                                // create job message
                                AgentCommonMessage<?> jobMessage = buildJobMessage(hostTarget.getJobDetailId(), hostTarget.getHostId(),
                                        "ExecuteCmd", messageHeader.getTraceId(), hostExecCmdJob);
                                // send job message to agent
                                sendJobToAgent(agentSession, jobReq, jobMessage, hostTarget.getJobDetailId(), messageHeader.getTraceId());
                            });
                });
                break;
            default:
                throw new UnknownJobException();
        }
    }

    private List<String> hostTargetToHostIdList(List<JobParams.HostTarget> hostTargets) {
        return hostTargets.stream().map(JobParams.HostTarget::getHostId).toList();
    }
}
