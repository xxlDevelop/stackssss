package org.yx.hoststack.center.jobs;


import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.yx.hoststack.center.cache.ServiceDetailCache;
import org.yx.hoststack.center.cache.model.ServiceDetailCacheModel;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.enums.JobStatusEnum;
import org.yx.hoststack.center.common.req.channel.SendChannelReq;
import org.yx.hoststack.center.entity.JobDetail;
import org.yx.hoststack.center.entity.JobInfo;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.queue.message.JobReportMessage;
import org.yx.hoststack.center.service.biz.ServerCacheInfoServiceBiz;
import org.yx.hoststack.center.service.center.CenterService;
import org.yx.hoststack.center.ws.session.Session;
import org.yx.hoststack.center.ws.session.SessionAttrKeys;
import org.yx.hoststack.center.ws.session.SessionManager;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.SpringContextHolder;
import org.yx.lib.utils.util.StringUtil;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BaseJob {
    @Autowired
    protected JobProcessService jobProcessService;
    @Autowired
    protected TransactionTemplate transactionTemplate;
    @Autowired
    protected CenterService centerService;
    @Autowired
    protected ServerCacheInfoServiceBiz serverCacheInfoServiceBiz;
    @Autowired
    protected ServiceDetailCache serviceDetailCache;
    @Autowired
    protected SessionManager sessionManager;


    /**
     * save job to db with transaction
     * @param jobId             jobId
     * @param jobCmd            jobCmd
     * @param jobParams         jobParams
     * @param externalParams    externalParams
     * @param jobDetailList     jobDetailList
     */
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

    /**
     * save job to db without transaction
     * @param jobId             jobId
     * @param jobCmd            jobCmd
     * @param jobParams         jobParams
     * @param externalParams    externalParams
     * @param jobDetailList     jobDetailList
     */
    protected void persistenceJob(String jobId, JobInnerCmd<?> jobCmd, JSONObject jobParams,
                                  String externalParams, List<JobDetail> jobDetailList) {
        JobInfo jobInfo = JobInfo.builder()
                .jobId(jobId)
                .jobType(jobCmd.getJobType().getName())
                .jobSubType(jobCmd.getJobSubType().getName())
                .runOrder(jobCmd.getRunOrder())
                .jobStatus(JobStatusEnum.PROCESSING.getName())
                .jobProgress(0)
                .jobDetailNum(jobDetailList == null ? 0 : jobDetailList.size())
                .tenantId(jobCmd.getTenantId())
                .jobParams(jobParams != null ? jobParams.toJSONString() : null)
                .externalParams(externalParams)
                .createAt(new Timestamp(System.currentTimeMillis()))
                .nextJobId(jobCmd.getNextJobId())
                .rootJobId(jobCmd.getRootJobId())
                .jobInnerCmd(JSON.toJSONString(jobCmd))
                .build();
        jobProcessService.insertJob(jobInfo);
        if (jobDetailList != null && !jobDetailList.isEmpty()) {
            jobProcessService.saveBatchDetailJobs(jobDetailList);
        }
    }

    /**
     * send job to idc or relay
     * @param edgeId        edgeId
     * @param jobCmd        jobCmd
     * @param jobParams     jobParams
     * @return R
     */
    protected R<?> sendJobToEdge(String edgeId, JobInnerCmd<?> jobCmd, ByteString jobParams) {
        List<ServiceDetailCacheModel> serviceDetailCacheModels = serviceDetailCache.getServiceDetailsByEdge(edgeId);
        CommonMessageWrapper.Body jobMessageBody = buildJobSendMessageBody(jobCmd, jobParams);
        String traceId = UUID.fastUUID().toString(true);
        Optional<Session> idcSession =
                sessionManager.getRandomSession(serviceDetailCacheModels.stream().map(ServiceDetailCacheModel::getServiceId).collect(Collectors.toList()));
        if (idcSession.isPresent()) {
            idcSession.get().sendMsg(traceId, jobMessageBody, null, null);
            printStdSendJobLog(traceId, idcSession.get(), jobCmd, jobParams, R.ok());
            return R.ok();
        } else {
            ServiceDetailCacheModel serviceDetailCacheModel = serviceDetailCacheModels.getFirst();
            return centerService.fetchChannelFromRemote(SendChannelReq.builder()
                    .traceId(traceId)
                    .serviceId(serviceDetailCacheModel.getServiceId())
                    .hostId("")
                    .msg(jobMessageBody.toByteArray())
                    .build());
        }
    }

    /**
     * send job to host or container
     * @param agentId       agentId
     * @param jobCmd        jobCmd
     * @param jobParams     jobParams
     * @return R
     */
    protected R<?> sendJobToAgent(String agentId, JobInnerCmd<?> jobCmd, ByteString jobParams) {
        CommonMessageWrapper.Body jobMessageBody = buildJobSendMessageBody(jobCmd, jobParams);
        return centerService.sendMsgToLocalOrRemoteChannel(SendChannelReq.builder()
                .serviceId("")
                .hostId(agentId)
                .msg(jobMessageBody.toByteArray())
                .build());
    }

    protected CommonMessageWrapper.Body buildJobSendMessageBody(JobInnerCmd<?> jobCmd, ByteString jobParams) {
        return CommonMessageWrapper.Body.newBuilder()
                .setPayload(C2EMessage.C2E_DoJobReq.newBuilder()
                        .setJobId(jobCmd.getJobId())
                        .setJobType(jobCmd.getJobType().getName())
                        .setJobSubType(jobCmd.getJobSubType().getName())
                        .setJobParams(jobParams)
                        .build().toByteString())
                .build();
    }

    protected R<SendJobResult> buildSendResult(R<?> sendR, String jobId, List<String> jobDetailIds) {
        return R.<SendJobResult>builder()
                .code(sendR.getCode())
                .msg(sendR.getMsg())
                .data(SendJobResult.builder()
                        .jobId(jobId)
                        .totalJobCount(jobDetailIds == null ? 0 : jobDetailIds.size())
                        .success(sendR.getCode() == R.ok().getCode() ? jobDetailIds : null)
                        .fail(sendR.getCode() != R.ok().getCode() ? jobDetailIds : null)
                        .build())
                .build();
    }

    protected SendJobResult buildDefaultSendResult(String jobId, int totalJobCount) {
        SendJobResult sendJobResult = new SendJobResult();
        sendJobResult.setJobId(jobId);
        sendJobResult.setTotalJobCount(totalJobCount);
        sendJobResult.setSuccess(Lists.newArrayList());
        sendJobResult.setFail(Lists.newArrayList());
        return sendJobResult;
    }

    protected JSONObject buildJobResult(JobReportMessage reportMessage) {
        return new JSONObject()
                .fluentPut("code", reportMessage.getCode())
                .fluentPut("msg", reportMessage.getMsg())
                .fluentPut("output", reportMessage.getOutput());
    }

    /**
     * group agent for serviceId
     * @param agentIds agentId list
     * @return Map<ServiceId, List < AgentId>>
     */
    protected Map<String, List<String>> groupAgentIdForService(List<String> agentIds) {
        Map<String, List<String>> groupMap = Maps.newHashMap();
        for (String agentId : agentIds) {
            Optional<Session> agentSessionOpt = sessionManager.getSession(agentId);
            String serviceId;
            if (agentSessionOpt.isPresent()) {
                serviceId = StringUtil.isNotBlank(agentSessionOpt.get().getRelaySid()) ? agentSessionOpt.get().getRelaySid() : agentSessionOpt.get().getIdcSid();
            } else {
                serviceId = sessionManager.getSessionAttr(agentId, SessionAttrKeys.SERVICE_ID);
            }
            groupMap.compute(serviceId, (k, v) -> {
                if (v == null) {
                    return Lists.newArrayList(agentId);
                } else {
                    v.add(agentId);
                    return v;
                }
            });
        }
        return groupMap;
    }

    private void printStdSendJobLog(String traceId, Session session, JobInnerCmd<?> jobCmd, ByteString jobParams, R<?> sendResult) {
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.SEND_JOB)
                .p(LogFieldConstants.ACTION, CenterEvent.Action.SEND_JOB_TO_EDGE)
                .p(LogFieldConstants.TRACE_ID, traceId)
                .p(LogFieldConstants.TID, session.getTenantId())
                .p(HostStackConstants.IDC_SID, session.getIdcSid())
                .p(HostStackConstants.RELAY_SID, session.getRelaySid())
                .p(HostStackConstants.REGION, session.getRegion())
                .p(HostStackConstants.JOB_TYPE, jobCmd.getJobType().getName())
                .p(HostStackConstants.JOB_SUB_TYPE, jobCmd.getJobSubType().getName())
                .p(HostStackConstants.JOB_ID, jobCmd.getJobId())
                .p("NextJobId", jobCmd.getNextJobId())
                .p("RootJobId", jobCmd.getRootJobId())
                .p("RunOrder", jobCmd.getRunOrder())
                .p(LogFieldConstants.Code, sendResult.getCode())
                .p(LogFieldConstants.ERR_MSG, sendResult.getMsg());
        kvLogger.i();
        if (kvLogger.isDebug()) {
            kvLogger.p("JobCmd", JSON.toJSONString(jobCmd))
                    .p("JobParams", Base64.encode(jobParams.toByteArray()))
                    .d();
        }
    }
}
