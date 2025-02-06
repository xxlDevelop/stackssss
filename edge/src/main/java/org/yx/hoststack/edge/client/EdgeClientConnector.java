package org.yx.hoststack.edge.client;

import cn.hutool.core.lang.UUID;
import cn.hutool.crypto.digest.MD5;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import org.yx.hoststack.common.syscode.EdgeSysCode;
import org.yx.hoststack.edge.client.controller.jobs.JobCacheData;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.SendMsgCallback;
import org.yx.hoststack.edge.queue.message.HostHeartMessage;
import org.yx.hoststack.edge.server.RunMode;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.hoststack.protocol.ws.agent.req.HostHeartbeatReq;
import org.yx.hoststack.protocol.ws.agent.req.HostInitializeReq;
import org.yx.hoststack.protocol.ws.server.E2CMessage;
import org.yx.hoststack.protocol.ws.server.JobResult;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.util.StringPool;
import org.yx.lib.utils.util.StringUtil;

import java.util.List;

public class EdgeClientConnector extends EdgeClientConnectorBase {
    private static volatile EdgeClientConnector instance;

    private EdgeClientConnector() {
        super();
    }

    public static EdgeClientConnector getInstance() {
        if (instance == null) {
            synchronized (EdgeClientConnector.class) {
                if (instance == null) {
                    instance = new EdgeClientConnector();
                }
            }
        }
        return instance;
    }

    public void edgeRegister() {
        sendMsg(buildSendMessage(ProtoMethodId.EdgeRegister.getValue(),
                        E2CMessage.E2C_EdgeRegisterReq.newBuilder()
                                .setServiceIp(EdgeContext.ServiceIp)
                                .setVersion(EdgeContext.ProjectVersion)
                                .setServiceType(EdgeContext.RunMode)
                                .build().toByteString(), EdgeContext.RunMode.equals(RunMode.IDC) ? EdgeContext.IdcServiceId : EdgeContext.RelayServiceId),
                null, null);
    }

    public void hostInitialize(String hostId, String hostToken, HostInitializeReq hostInitializeReq, String traceId, SendMsgCallback successCallback, SendMsgCallback failCallback) {
        List<E2CMessage.GpuInfo> gpuInfoList = Lists.newArrayList();
        for (HostInitializeReq.GpuInfo gpuInfo : hostInitializeReq.getGpuList()) {
            gpuInfoList.add(E2CMessage.GpuInfo.newBuilder()
                    .setGpuType(gpuInfo.getGpuType())
                    .setGpuManufacturer(gpuInfo.getGpuManufacturer())
                    .setGpuMem(gpuInfo.getGpuMem())
                    .setGpuBusType(gpuInfo.getGpuBusType())
                    .setGpuDeviceId(gpuInfo.getGpuDeviceId())
                    .setGpuBusId(gpuInfo.getGpuBusId())
                    .build());
        }
        List<E2CMessage.NetCardInfo> netCardInfoList = Lists.newArrayList();
        for (HostInitializeReq.NetCardInfo netCardInfo : hostInitializeReq.getNetcardList()) {
            netCardInfoList.add(E2CMessage.NetCardInfo.newBuilder()
                    .setNetCardName(netCardInfo.getNetcardName())
                    .setNetCardType(netCardInfo.getNetcardType())
                    .setNetCardLinkSpeed(netCardInfo.getNetcardLinkSpeed())
                    .build());
        }
        E2CMessage.E2C_HostInitializeReq hostInitialize = E2CMessage.E2C_HostInitializeReq.newBuilder()
                .setAgentStartTs(hostInitializeReq.getAgentStartTs())
                .setAgentType(hostInitializeReq.getAgentType())
                .setResourcePool(hostInitializeReq.getResourcePool())
                .setRuntimeEnv(hostInitializeReq.getRuntimeEnv())
                .setOsStartTs(hostInitializeReq.getOsStartTs())
                .setDevSn(hostInitializeReq.getDevSn())
                .setOsType(hostInitializeReq.getOsType())
                .setOsVersion(hostInitializeReq.getOsVersion())
                .setAgentVersion(hostInitializeReq.getAgentVersion())
                .setOsMem(hostInitializeReq.getOsMem())
                .setLocalIp(hostInitializeReq.getLocalIp())
                .setDisk(hostInitializeReq.getDisk())
                .setHostId(hostId)
                .setDetailedId(hostInitializeReq.getDetailedId())
                .setProxy(hostInitializeReq.getProxy())
                .addAllGpuList(gpuInfoList)
                .addAllNetCardList(netCardInfoList)
                .setXToken(hostToken)
                .build();
        sendMsg(buildSendMessage(ProtoMethodId.HostInitialize.getValue(), hostInitialize.toByteString(), traceId), successCallback, failCallback);
    }

    public void hostHb(List<HostHeartMessage> hostHeartMessages) {
        List<E2CMessage.HostHbData> hostHbDataList = Lists.newArrayList();
        for (HostHeartMessage hostHeartMessage : hostHeartMessages) {
            E2CMessage.HostStatus hostStatus = E2CMessage.HostStatus.newBuilder()
                    .setCpuUsage(hostHeartMessage.getHostHeartbeatReq().getHostStatus().getCpuUsage())
                    .setMemoryUsage(hostHeartMessage.getHostHeartbeatReq().getHostStatus().getMemoryUsage())
                    .build();
            List<E2CMessage.VmStatus> vmStatusList = Lists.newArrayList();
            if (hostHeartMessage.getHostHeartbeatReq().getVmStatus() != null) {
                for (HostHeartbeatReq.VmStatus vmStatus : hostHeartMessage.getHostHeartbeatReq().getVmStatus()) {
                    vmStatusList.add(E2CMessage.VmStatus.newBuilder()
                            .setVmName(vmStatus.getVmName())
                            .setImageVer(vmStatus.getImageVer())
                            .setCid(vmStatus.getCid())
                            .setRunning(vmStatus.isRunning())
                            .build());
                }
            }
            E2CMessage.HostHbData hostHbData = E2CMessage.HostHbData.newBuilder()
                    .setHostId(hostHeartMessage.getHostId())
                    .setAgentType(hostHeartMessage.getAgentType())
                    .setHostStatus(hostStatus)
                    .addAllVmStatus(vmStatusList)
                    .build();
            hostHbDataList.add(hostHbData);
        }
        E2CMessage.E2C_HostHeartbeatReq e2cHostHeartbeat = E2CMessage.E2C_HostHeartbeatReq.newBuilder()
                .addAllHbData(hostHbDataList)
                .build();
        sendMsg(buildSendMessage(ProtoMethodId.HostHeartbeat.getValue(), e2cHostHeartbeat.toByteString(), UUID.fastUUID().toString()), null, null);
    }

    public void startHb(int hbInterval) {
        super.startHeartbeat(hbInterval);
    }

    public void sendHostExit(String hostId, String agentType) {
        E2CMessage.E2C_HostExitReq hostExitReq = E2CMessage.E2C_HostExitReq.newBuilder()
                .setHostId(hostId)
                .setAgentType(agentType)
                .build();
        sendMsg(buildSendMessage(ProtoMethodId.HostExit.getValue(), hostExitReq.toByteString(), UUID.fastUUID().toString()), null, null);
    }

    public void sendIdcExit(String idcId) {
        E2CMessage.E2C_IdcExitReq idcExitReq = E2CMessage.E2C_IdcExitReq.newBuilder()
                .setIdcSid(idcId)
                .build();
        sendMsg(buildSendMessage(ProtoMethodId.IdcExit.getValue(), idcExitReq.toByteString(), UUID.fastUUID().toString()), null, null);
    }

    public void sendJobNotifyReport(List<AgentCommonMessage<?>> agentReportList, String traceId,
                                    SendMsgCallback successCallback, SendMsgCallback failCallback) {
        E2CMessage.E2C_JobReportReq.Builder jobReportReqBuilder = E2CMessage.E2C_JobReportReq.newBuilder();
        for (AgentCommonMessage<?> agentReport : agentReportList) {
            String jobFullId = agentReport.getJobId();
            JobCacheData jobCacheData = jobCacheService.getJob(jobFullId);
            String jobId = "";
            String jobDetailId = "";
            String jobType = "";
            String jobSubType = "";

            if (jobCacheData != null) {
                jobId = jobCacheData.getJobId();
                jobDetailId = jobCacheData.getJobDetailId();
                jobType = jobCacheData.getJobType();
                jobSubType = jobCacheData.getJobSubType();
            } else {
                if (jobFullId.contains(StringPool.DASH)) {
                    jobDetailId = agentReport.getJobId();
                    jobId = jobDetailId.split(StringPool.DASH)[0];
                } else {
                    jobDetailId = "";
                    jobId = agentReport.getJobId();
                }
            }
            String jobStatus = agentReport.getStatus();
            int jobProgress = agentReport.getProgress();
            int jobCode = agentReport.getCode();
            String jobMessage = agentReport.getMsg();
            String jobTraceId = agentReport.getTraceId();

            jobReportReqBuilder.addItems(
                    E2CMessage.JobReportItem.newBuilder()
                            .setTraceId(jobTraceId)
                            .setJobId(jobId)
                            .setJobType(jobType)
                            .setJobSubType(jobSubType)
                            .setJobResult(JobResult.JobTargetResult.newBuilder()
                                    .addTargetResult(JobResult.TargetResult.newBuilder()
                                            .setJobDetailId(jobDetailId)
                                            .setStatus(jobStatus)
                                            .setProgress(jobProgress)
                                            .setCode(jobCode)
                                            .setMsg(StringUtil.isBlank(jobMessage) ? "" : jobMessage)
                                            .setOutput(agentReport.getData() == null ? "" : JSON.toJSONString(agentReport.getData()))
                                            .build())
                                    .build().toByteString())
                            .build());
        }

        sendMsg(buildResultMessage(ProtoMethodId.JobReport.getValue(), EdgeSysCode.Success.getValue(), "", jobReportReqBuilder.build().toByteString(), traceId),
                successCallback, failCallback);
    }

    public void sendResultToUpstream(int methodId, int code, String msg, ByteString payload, String traceId) {
        sendMsg(buildResultMessage(methodId, code, msg, payload, traceId), null, null);
    }

    public void sendJobFailedToUpstream(String jobId, String jobDetailId,
                                        int code, String errorMsg, String traceId) {
        E2CMessage.E2C_JobReportReq.Builder jobReportReqBuilder = E2CMessage.E2C_JobReportReq.newBuilder();
        if (StringUtil.isNotBlank(jobDetailId)) {
            jobReportReqBuilder.addItems(
                    E2CMessage.JobReportItem.newBuilder()
                            .setTraceId(traceId)
                            .setJobId(jobId)
                            .setJobResult(JobResult.JobTargetResult.newBuilder()
                                    .addTargetResult(JobResult.TargetResult.newBuilder()
                                            .setJobDetailId(jobDetailId)
                                            .setStatus("fail")
                                            .setProgress(0)
                                            .setCode(code)
                                            .setMsg(errorMsg)
                                            .build())
                                    .build().toByteString())
                            .build());
        }
        sendMsg(buildResultMessage(ProtoMethodId.JobReport.getValue(),
                EdgeSysCode.NotFoundAgentSession.getValue(), EdgeSysCode.NotFoundAgentSession.getMsg(),
                jobReportReqBuilder.build().toByteString(), traceId), null, null);
    }
}
