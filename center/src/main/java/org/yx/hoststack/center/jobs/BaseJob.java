package org.yx.hoststack.center.jobs;


import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.yx.hoststack.center.common.dto.ServiceDetailDTO;
import org.yx.hoststack.center.common.enums.JobStatusEnum;
import org.yx.hoststack.center.common.req.channel.SendChannelReq;
import org.yx.hoststack.center.entity.Host;
import org.yx.hoststack.center.entity.JobDetail;
import org.yx.hoststack.center.entity.JobInfo;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.service.JobDetailService;
import org.yx.hoststack.center.service.JobInfoService;
import org.yx.hoststack.center.service.biz.ServerCacheInfoServiceBiz;
import org.yx.hoststack.center.service.center.CenterService;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.util.StringUtil;

import java.sql.Timestamp;
import java.util.List;

public class BaseJob {
    protected final JobInfoService jobInfoService;
    protected final JobDetailService jobDetailService;
    protected final TransactionTemplate transactionTemplate;
    private final CenterService centerService;
    protected final ServerCacheInfoServiceBiz serverCacheInfoServiceBiz;

    public BaseJob(JobInfoService jobInfoService,
                   JobDetailService jobDetailService,
                   CenterService centerService,
                   ServerCacheInfoServiceBiz serverCacheInfoServiceBiz,
                   TransactionTemplate transactionTemplate) {
        this.jobInfoService = jobInfoService;
        this.jobDetailService = jobDetailService;
        this.transactionTemplate = transactionTemplate;
        this.centerService = centerService;
        this.serverCacheInfoServiceBiz = serverCacheInfoServiceBiz;
    }

    protected void safetyPersistenceJob(String jobId, JobInnerCmd<?> jobCmd, JSONObject jobParams,
                                        String externalParams, List<JobDetail> jobDetailList) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    persistenceJob(jobId, jobCmd, jobParams, externalParams, jobDetailList);
                } catch (Exception ex) {
                    status.setRollbackOnly();
                    throw ex;
                }
            }
        });
    }

    protected void persistenceJob(String jobId, JobInnerCmd<?> jobCmd, JSONObject jobParams,
                                  String externalParams, List<JobDetail> jobDetailList) {
        int process = 0;
        if (jobDetailList != null) {
            process = (int) jobDetailList.stream().mapToInt(JobDetail::getJobProgress).average().orElse(0);
        }
        JobInfo jobInfo = JobInfo.builder()
                .jobId(jobId)
                .jobType(jobCmd.getJobType().getName())
                .jobSubType(jobCmd.getJobSubType().getName())
                .runOrder(jobCmd.getRunOrder())
                .jobStatus(JobStatusEnum.PROCESSING.getName())
                .jobProgress(process)
                .zone(jobCmd.getZone())
                .region(jobCmd.getRegion())
                .idc(jobCmd.getIdc())
                .relay(jobCmd.getRelay())
                .jobDetailNum(jobDetailList == null ? 0 : jobDetailList.size())
                .tenantId(jobCmd.getTenantId())
                .jobParams(jobParams != null ? jobParams.toJSONString() : null)
                .externalParams(externalParams)
                .createAt(new Timestamp(System.currentTimeMillis()))
                .nextJobId(jobCmd.getNextJobId())
                .rootJobId(jobCmd.getRootJobId())
                .build();
        jobInfoService.insert(jobInfo);
        if (jobDetailList != null && !jobDetailList.isEmpty()) {
            jobDetailService.saveBatch(jobDetailList);
        }
    }

    protected void sendJobToEdge(JobInnerCmd<?> jobCmd, ByteString jobParams) {
        ServiceDetailDTO serviceDetail = getService(jobCmd.getIdc(), jobCmd.getRelay());
        CommonMessageWrapper.CommonMessage commonMessage = buildJobMessage(jobCmd, serviceDetail, jobParams);
        centerService.sendMsgToLocalChannel(SendChannelReq.builder()
                .serviceId(serviceDetail.getServiceId())
                .hostId("")
                .msg(commonMessage.toByteArray())
                .build());
    }

    protected void sendJobToAgent(JobInnerCmd<?> jobCmd, String agentId, ByteString jobParams) {
        sendJobToAgent(jobCmd, Lists.newArrayList(agentId), jobParams);
    }

    protected void sendJobToAgent(JobInnerCmd<?> jobCmd, List<String> agentIds, ByteString jobParams) {
        for (String agentId : agentIds) {
            Host host = serverCacheInfoServiceBiz.getHostInfo(agentId);
            ServiceDetailDTO serviceDetail = getService(host.getIdc(), host.getRelay());
            CommonMessageWrapper.CommonMessage commonMessage = buildJobMessage(jobCmd, serviceDetail, jobParams);
            centerService.sendMsgToLocalChannel(SendChannelReq.builder()
                    .serviceId("")
                    .hostId(host.getHostId())
                    .msg(commonMessage.toByteArray())
                    .build());
        }
    }

    protected ServiceDetailDTO getService(String idcId, String relayId) {
        ServiceDetailDTO serviceDetail;
        if (StringUtil.isNotBlank(idcId) && StringUtil.isNotBlank(relayId)) {
            serviceDetail = serverCacheInfoServiceBiz.getServiceInfo(relayId);
        } else if (StringUtil.isNotBlank(idcId)) {
            serviceDetail = serverCacheInfoServiceBiz.getServiceInfo(idcId);
        } else {
            serviceDetail = serverCacheInfoServiceBiz.getServiceInfo(relayId);
        }
        return serviceDetail;
    }

    protected CommonMessageWrapper.CommonMessage buildJobMessage(JobInnerCmd<?> jobCmd, ServiceDetailDTO serviceDetail, ByteString jobParams) {
        return CommonMessageWrapper.CommonMessage.newBuilder()
                .setHeader(CommonMessageWrapper.Header.newBuilder()
                        .setLinkSide(CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE)
                        .setTraceId(jobCmd.getJobId())
                        .setProtocolVer(CommonMessageWrapper.ENUM_PROTOCOL_VER.PROTOCOL_V1_VALUE)
                        .setZone(serviceDetail.getZone())
                        .setRegion(serviceDetail.getRegion())
                        .setRelaySid(serviceDetail.getRelaySid())
                        .setIdcSid(serviceDetail.getIdcSid())
                        .setTimestamp(System.currentTimeMillis())
                        .setMethId(ProtoMethodId.DoJob.getValue())
                        .setTenantId(jobCmd.getTenantId())
                        .build())
                .setBody(CommonMessageWrapper.Body.newBuilder()
                        .setPayload(C2EMessage.C2E_DoJobReq.newBuilder()
                                .setJobId(jobCmd.getJobId())
                                .setJobType(jobCmd.getJobType().getName())
                                .setJobSubType(jobCmd.getJobSubType().getName())
                                .setJobParams(jobParams)
                                .build().toByteString())
                        .build())
                .build();
    }
}
