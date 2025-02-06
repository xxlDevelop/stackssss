package org.yx.hoststack.center.jobs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.dto.ServiceDetailDTO;
import org.yx.hoststack.center.common.enums.JobStatusEnum;
import org.yx.hoststack.center.common.enums.JobSubTypeEnum;
import org.yx.hoststack.center.common.enums.SysCode;
import org.yx.hoststack.center.entity.Container;
import org.yx.hoststack.center.entity.JobDetail;
import org.yx.hoststack.center.entity.Volume;
import org.yx.hoststack.center.entity.VolumeMountRel;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.jobs.cmd.host.HostExecCmdData;
import org.yx.hoststack.center.jobs.cmd.host.HostResetCmdData;
import org.yx.hoststack.center.jobs.cmd.host.HostUpdateConfigCmdData;
import org.yx.hoststack.center.queue.message.JobReportMessage;
import org.yx.hoststack.center.service.ContainerService;
import org.yx.hoststack.center.service.VolumeMountRelService;
import org.yx.hoststack.center.service.VolumeService;
import org.yx.hoststack.center.service.biz.ServerCacheInfoServiceBiz;
import org.yx.hoststack.center.service.center.CenterService;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.protocol.ws.server.JobParams;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.SpringContextHolder;
import org.yx.lib.utils.util.StringPool;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Service("host")
public class HostJob extends BaseJob implements IJob {

    @Override
    public String createJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case RESET -> reset(jobCmd, false);
            case UPDATE_CONFIG -> updateConfig(jobCmd, false);
            case EXEC_CMD -> execCmd(jobCmd, false);
            default -> "";
        };
    }

    @Override
    public String safetyCreateJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case RESET -> reset(jobCmd, true);
            case UPDATE_CONFIG -> updateConfig(jobCmd, true);
            case EXEC_CMD -> execCmd(jobCmd, true);
            default -> "";
        };
    }

    @Override
    public R<SendJobResult> sendJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case RESET -> sendReset(jobCmd);
            case UPDATE_CONFIG -> sendUpdateConfig(jobCmd);
            case EXEC_CMD -> sendExecCmd(jobCmd);
            default -> R.failed(SysCode.x00000700.getValue(), SysCode.x00000700.getMsg());
        };
    }

    @Override
    public void processJobReportResult(JobReportMessage reportMessage) {
        switch (JobSubTypeEnum.fromString(reportMessage.getJobSubType())) {
            case RESET, UPDATE_CONFIG, EXEC_CMD:
                processJobResult(reportMessage);
                break;
            default:
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, CenterEvent.PROCESS_JOB_NOTIFY)
                        .p(LogFieldConstants.ACTION, CenterEvent.Action.JOB_NOTIFY)
                        .p(LogFieldConstants.TRACE_ID, reportMessage.getTraceId())
                        .p(LogFieldConstants.ERR_MSG, "Unknown jobSubType")
                        .p(HostStackConstants.JOB_TYPE, reportMessage.getJobType())
                        .p(HostStackConstants.JOB_SUB_TYPE, reportMessage.getJobSubType())
                        .p(HostStackConstants.JOB_ID, reportMessage.getJobId())
                        .p(HostStackConstants.JOB_DETAIL_ID, reportMessage.getJobDetailId())
                        .p("Output", reportMessage.getOutput())
                        .w();
        }
    }

    public String reset(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        HostResetCmdData hostResetCmdData = (HostResetCmdData) jobCmd.getJobData();
        List<String> hotIdArray = hostResetCmdData.getHostIds();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("HostIds", String.join(StringPool.COMMA, hotIdArray))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String hostId : hotIdArray) {
            ServiceDetailDTO serviceDetail = serverCacheInfoServiceBiz.getAgentInfo(hostId);

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + hostId)
                    .jobHost(hostId)
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(0)
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .idc(serviceDetail.getIdc())
                    .relay(serviceDetail.getRelay())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
                    .jobParams("{\"hostId\": \"" + hostId + "\"}")
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        return persistence(safety, jobId, jobCmd, jobDetailList,
                successJobId -> kvLogger.i(), error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
    }

    public String updateConfig(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        HostUpdateConfigCmdData configCmdData = (HostUpdateConfigCmdData) jobCmd.getJobData();
        List<HostUpdateConfigCmdData.HostConfig> configs = configCmdData.getConfigs();
        List<String> hotIdArray = configCmdData.getHostIds();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("HostIds", String.join(StringPool.COMMA, hotIdArray))
                .p("Configs", JSON.toJSONString(configs))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String hostId : hotIdArray) {
            ServiceDetailDTO serviceDetail = serverCacheInfoServiceBiz.getAgentInfo(hostId);

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + hostId)
                    .jobHost(hostId)
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(0)
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .idc(serviceDetail.getIdc())
                    .relay(serviceDetail.getRelay())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
                    .jobParams("{\"hostId\":\"" + hostId + "\",\"config\":\"" + JSON.toJSONString(configs) + "\"}")
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        return persistence(safety, jobId, jobCmd, jobDetailList,
                successJobId -> kvLogger.i(), error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
    }

    public String execCmd(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        HostExecCmdData execCmdData = (HostExecCmdData) jobCmd.getJobData();
        List<String> hotIdArray = execCmdData.getHostIds();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("HostIds", String.join(StringPool.COMMA, hotIdArray))
                .p("Script", execCmdData.getScript())
                .p(HostStackConstants.JOB_ID, jobId);

        List<JobDetail> jobDetailList = Lists.newArrayList();

        for (String hostId : hotIdArray) {
            ServiceDetailDTO serviceDetail = serverCacheInfoServiceBiz.getAgentInfo(hostId);

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + hostId)
                    .jobHost(hostId)
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(0)
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .idc(serviceDetail.getIdc())
                    .relay(serviceDetail.getRelay())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
                    .jobParams("{\"hostId\":\"" + hostId + "\",\"script\":\"" + execCmdData.getScript() + "\"}")
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        return persistence(safety, jobId, jobCmd, jobDetailList,
                successJobId -> kvLogger.i(), error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
    }

    private R<SendJobResult> sendReset(JobInnerCmd<?> jobCmd) {
        String jobId = jobCmd.getJobId();
        HostResetCmdData hostResetCmdData;
        if (jobCmd.getJobData() instanceof HostResetCmdData) {
            hostResetCmdData = (HostResetCmdData) jobCmd.getJobData();
        } else {
            hostResetCmdData = ((JSONObject) jobCmd.getJobData()).toJavaObject(HostResetCmdData.class);
        }

        // build default result
        SendJobResult sendJobResult = buildDefaultSendResult(jobId, hostResetCmdData.getHostIds().size());
        // group hostId and service rel
        Map<String, List<String>> agentServiceGroupMap = groupAgentIdForService(hostResetCmdData.getHostIds());
        // send job to edge by service
        for (String serviceId : agentServiceGroupMap.keySet()) {
            List<JobParams.HostTarget> targetList = Lists.newArrayList();
            for (String hostId : agentServiceGroupMap.get(serviceId)) {
                String jobDetailId = jobId + StringPool.DASH + hostId;

                targetList.add(JobParams.HostTarget.newBuilder()
                        .setHostId(hostId)
                        .setJobDetailId(jobDetailId)
                        .build());
            }
            JobParams.HostReset jobParams = JobParams.HostReset.newBuilder()
                    .addAllTarget(targetList)
                    .build();
            List<String> jobDetailIds = targetList.stream().map(JobParams.HostTarget::getJobDetailId).toList();
            R<?> sendR = sendJobToEdge(serviceId, jobCmd, jobParams.toByteString());
            if (sendR.getCode() == R.ok().getCode()) {
                sendJobResult.getSuccess().addAll(jobDetailIds);
            } else {
                sendJobResult.getFail().addAll(jobDetailIds);
            }
        }
        boolean isAllSuccess = sendJobResult.getSuccess().size() == sendJobResult.getTotalJobCount();
        return R.<SendJobResult>builder()
                .code(isAllSuccess ? 0 : SysCode.x00000701.getValue())
                .msg(isAllSuccess ? null : SysCode.x00000701.getMsg())
                .data(sendJobResult)
                .build();
    }

    private R<SendJobResult> sendUpdateConfig(JobInnerCmd<?> jobCmd) {
        String jobId = jobCmd.getJobId();
        HostUpdateConfigCmdData configCmdData;
        if (jobCmd.getJobData() instanceof HostUpdateConfigCmdData) {
            configCmdData = (HostUpdateConfigCmdData) jobCmd.getJobData();
        } else {
            configCmdData = ((JSONObject) jobCmd.getJobData()).toJavaObject(HostUpdateConfigCmdData.class);
        }

        // build to update config proto
        List<JobParams.HostToUpdateConfigDetail> configDetails = Lists.newArrayList();
        for (HostUpdateConfigCmdData.HostConfig config : configCmdData.getConfigs()) {
            configDetails.add(JobParams.HostToUpdateConfigDetail.newBuilder()
                    .setType(config.getType())
                    .putAllContext(config.getContext())
                    .build());
        }
        // build default result
        SendJobResult sendJobResult = buildDefaultSendResult(jobId, configCmdData.getHostIds().size());
        // group hostId and service rel
        Map<String, List<String>> agentServiceGroupMap = groupAgentIdForService(configCmdData.getHostIds());
        // send job to edge by service
        for (String serviceId : agentServiceGroupMap.keySet()) {
            List<JobParams.HostTarget> targetList = Lists.newArrayList();
            for (String hostId : agentServiceGroupMap.get(serviceId)) {
                String jobDetailId = jobId + StringPool.DASH + hostId;

                targetList.add(JobParams.HostTarget.newBuilder()
                        .setHostId(hostId)
                        .setJobDetailId(jobDetailId)
                        .build());
            }
            JobParams.HostUpdateConfig jobParams = JobParams.HostUpdateConfig.newBuilder()
                    .addAllConfig(configDetails)
                    .addAllTarget(targetList)
                    .build();
            List<String> jobDetailIds = targetList.stream().map(JobParams.HostTarget::getJobDetailId).toList();
            R<?> sendR = sendJobToEdge(serviceId, jobCmd, jobParams.toByteString());
            if (sendR.getCode() == R.ok().getCode()) {
                sendJobResult.getSuccess().addAll(jobDetailIds);
            } else {
                sendJobResult.getFail().addAll(jobDetailIds);
            }
        }
        boolean isAllSuccess = sendJobResult.getSuccess().size() == sendJobResult.getTotalJobCount();
        return R.<SendJobResult>builder()
                .code(isAllSuccess ? 0 : SysCode.x00000701.getValue())
                .msg(isAllSuccess ? null : SysCode.x00000701.getMsg())
                .data(sendJobResult)
                .build();
    }

    private R<SendJobResult> sendExecCmd(JobInnerCmd<?> jobCmd) {
        String jobId = jobCmd.getJobId();
        HostExecCmdData execCmdData;
        if (jobCmd.getJobData() instanceof HostExecCmdData) {
            execCmdData = (HostExecCmdData) jobCmd.getJobData();
        } else {
            execCmdData = ((JSONObject) jobCmd.getJobData()).toJavaObject(HostExecCmdData.class);
        }

        // build default result
        SendJobResult sendJobResult = buildDefaultSendResult(jobId, execCmdData.getHostIds().size());
        // group hostId and service rel
        Map<String, List<String>> agentServiceGroupMap = groupAgentIdForService(execCmdData.getHostIds());
        // send job to edge by service
        for (String serviceId : agentServiceGroupMap.keySet()) {
            List<JobParams.HostTarget> targetList = Lists.newArrayList();
            for (String hostId : agentServiceGroupMap.get(serviceId)) {
                String jobDetailId = jobId + StringPool.DASH + hostId;

                targetList.add(JobParams.HostTarget.newBuilder()
                        .setHostId(hostId)
                        .setJobDetailId(jobDetailId)
                        .build());
            }
            JobParams.HostExecCmd jobParams = JobParams.HostExecCmd.newBuilder()
                    .setScript(execCmdData.getScript())
                    .addAllTarget(targetList)
                    .build();
            List<String> jobDetailIds = targetList.stream().map(JobParams.HostTarget::getJobDetailId).toList();
            R<?> sendR = sendJobToEdge(serviceId, jobCmd, jobParams.toByteString());
            if (sendR.getCode() == R.ok().getCode()) {
                sendJobResult.getSuccess().addAll(jobDetailIds);
            } else {
                sendJobResult.getFail().addAll(jobDetailIds);
            }
        }
        boolean isAllSuccess = sendJobResult.getSuccess().size() == sendJobResult.getTotalJobCount();
        return R.<SendJobResult>builder()
                .code(isAllSuccess ? 0 : SysCode.x00000701.getValue())
                .msg(isAllSuccess ? null : SysCode.x00000701.getMsg())
                .data(sendJobResult)
                .build();
    }

    private String persistence(boolean safety, String jobId, JobInnerCmd<?> jobCmd, List<JobDetail> jobDetailList,
                               Consumer<String> consumer, Consumer<Exception> exceptionConsumer) {
        if (safety) {
            try {
                safetyPersistenceJob(jobId, jobCmd, null, "", jobDetailList);
                consumer.accept(jobId);
                return jobId;
            } catch (Exception ex) {
                exceptionConsumer.accept(ex);
                return "";
            }
        } else {
            persistenceJob(jobId, jobCmd, null, "", jobDetailList);
            consumer.accept(jobId);
            return jobId;
        }
    }

    private void processJobResult(JobReportMessage reportMessage) {
        JobStatusEnum jobStatus = JobStatusEnum.fromString(reportMessage.getStatus());
        if (jobStatus == JobStatusEnum.PROCESSING) {
            jobProcessService.updateDetailProgress(reportMessage.getJobId(), reportMessage.getJobDetailId(), reportMessage.getProgress());
        } else {
            // if reset and success, to delete volume, mountRel, container
            if (reportMessage.getJobSubType().equalsIgnoreCase(JobSubTypeEnum.RESET.getName()) && jobStatus == JobStatusEnum.SUCCESS) {

                JobDetail jobDetail = jobProcessService.getJobDetail(reportMessage.getJobId(), reportMessage.getJobDetailId());
                String jobParams = jobDetail.getJobParams();
                String hostId = JSON.parseObject(jobParams).getString("hostId");

                // delete volume
                VolumeService volumeService = SpringContextHolder.getBean(VolumeService.class);
                VolumeMountRelService volumeMountRelService = SpringContextHolder.getBean(VolumeMountRelService.class);
                // delete mountRel
                volumeService.remove(Wrappers.lambdaQuery(Volume.class).eq(Volume::getHostId, hostId));
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, CenterEvent.HOST_RESET_EVENT)
                        .p(LogFieldConstants.ACTION, "DeleteVolume")
                        .p(LogFieldConstants.TRACE_ID, reportMessage.getTraceId())
                        .p("HostId", hostId)
                        .i();

                volumeMountRelService.remove(Wrappers.lambdaQuery(VolumeMountRel.class).eq(VolumeMountRel::getVolumeHost, hostId));
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, CenterEvent.HOST_RESET_EVENT)
                        .p(LogFieldConstants.ACTION, "DeleteVolumeMountRel")
                        .p(LogFieldConstants.TRACE_ID, reportMessage.getTraceId())
                        .p("HostId", hostId)
                        .i();

                // release container
                ContainerService containerService = SpringContextHolder.getBean(ContainerService.class);
                containerService.releaseContainerByHost(reportMessage.getTraceId(), hostId);
            }
            // complete job
            jobProcessService.completeJob(jobStatus, reportMessage.getJobId(), reportMessage.getJobDetailId(), buildJobResult(reportMessage), null);
        }
    }
}
