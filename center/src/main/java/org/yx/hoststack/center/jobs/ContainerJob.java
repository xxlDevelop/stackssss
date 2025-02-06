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
import org.yx.hoststack.center.entity.*;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.jobs.cmd.container.*;
import org.yx.hoststack.center.queue.message.JobReportMessage;
import org.yx.hoststack.center.service.ContainerCreateProfileService;
import org.yx.hoststack.center.service.ContainerNetConfigService;
import org.yx.hoststack.center.service.ContainerProxyConfigService;
import org.yx.hoststack.center.service.ContainerService;
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

@Service("container")
public class ContainerJob extends BaseJob implements IJob {

    @Override
    public String createJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case CREATE -> create(jobCmd, false);
            case UPGRADE -> upgrade(jobCmd, false);
            case UPDATE_PROFILE -> updateProfile(jobCmd, false);
            case START, SHUTDOWN, REBOOT, DROP -> ctrl(jobCmd.getJobSubType(), jobCmd, false);
            case EXEC_CMD -> execCmd(jobCmd, false);
            default -> "";
        };
    }

    @Override
    public String safetyCreateJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case CREATE -> create(jobCmd, true);
            case UPGRADE -> upgrade(jobCmd, true);
            case UPDATE_PROFILE -> updateProfile(jobCmd, true);
            case START, SHUTDOWN, REBOOT, DROP -> ctrl(jobCmd.getJobSubType(), jobCmd, true);
            case EXEC_CMD -> execCmd(jobCmd, true);
            default -> "";
        };
    }

    @Override
    public R<SendJobResult> sendJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case CREATE -> sendCreate(jobCmd);
            case UPGRADE -> sendUpgrade(jobCmd);
            case UPDATE_PROFILE -> sendUpdateProfile(jobCmd);
            case START, SHUTDOWN, REBOOT, DROP -> sendCtrl(jobCmd.getJobSubType(), jobCmd);
            case EXEC_CMD -> sendExecCmd(jobCmd);
            default -> R.failed(SysCode.x00000700.getValue(), SysCode.x00000700.getMsg());
        };
    }

    @Override
    public void processJobReportResult(JobReportMessage reportMessage) {
        switch (JobSubTypeEnum.fromString(reportMessage.getJobSubType())) {
            case CREATE:
                processCreateJobResult(reportMessage);
                break;
            case UPGRADE:
                processUpgradeJobResult(reportMessage);
                break;
            case UPDATE_PROFILE:
                processUpdateProfileJobResult(reportMessage);
                break;
            case START, SHUTDOWN, REBOOT:
                processCtrlJobResult(reportMessage);
                break;
            case DROP:
                processDropJobResult(reportMessage);
                break;
            case EXEC_CMD:
                processExecCmdJobResult(reportMessage);
                break;
            default:
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, CenterEvent.PROCESS_JOB_NOTIFY)
                        .p(LogFieldConstants.ACTION, CenterEvent.Action.JOB_NOTIFY)
                        .p(LogFieldConstants.ERR_MSG, "Unknown jobSubType")
                        .p(LogFieldConstants.TRACE_ID, reportMessage.getTraceId())
                        .p(HostStackConstants.JOB_TYPE, reportMessage.getJobType())
                        .p(HostStackConstants.JOB_SUB_TYPE, reportMessage.getJobSubType())
                        .p(HostStackConstants.JOB_ID, reportMessage.getJobId())
                        .p(HostStackConstants.JOB_DETAIL_ID, reportMessage.getJobDetailId())
                        .p("Output", reportMessage.getOutput())
                        .w();
        }
    }

    private String create(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        CreateContainerCmdData createContainerCmdData = (CreateContainerCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("CreateContainerInfo", JSON.toJSONString(createContainerCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        ServiceDetailDTO serviceDetail = serverCacheInfoServiceBiz.getAgentInfo(createContainerCmdData.getHostId());

        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (CreateContainerCmdData.ContainerProfileInfo containerProfileInfo : createContainerCmdData.getProfileInfoList()) {
            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("hostId", createContainerCmdData.getHostId())
                    .fluentPut("vmType", createContainerCmdData.getVmType())
                    .fluentPut("profileTemplateId", createContainerCmdData.getProfileTemplateId())
                    .fluentPut("image", createContainerCmdData.getImage())
                    .fluentPut("cid", containerProfileInfo.getCid())
                    .fluentPut("profile", containerProfileInfo.getProfile());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + containerProfileInfo.getCid())
                    .jobHost(containerProfileInfo.getCid())
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(0)
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .idc(serviceDetail.getIdc())
                    .relay(serviceDetail.getRelay())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        return persistence(safety, jobId, jobCmd, jobDetailList,
                successJobId -> kvLogger.i(), error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
    }

    private String upgrade(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        UpgradeContainerCmdData upgradeContainerCmdData = (UpgradeContainerCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("UpgradeContainerInfo", JSON.toJSONString(upgradeContainerCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        ServiceDetailDTO serviceDetail = serverCacheInfoServiceBiz.getAgentInfo(upgradeContainerCmdData.getHostId());

        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String cid : upgradeContainerCmdData.getCIds()) {
            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("hostId", upgradeContainerCmdData.getHostId())
                    .fluentPut("vmType", upgradeContainerCmdData.getVmType())
                    .fluentPut("image", upgradeContainerCmdData.getImage())
                    .fluentPut("cid", cid)
                    .fluentPut("hostId", upgradeContainerCmdData.getHostId());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + cid)
                    .jobHost(cid)
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(0)
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .idc(serviceDetail.getIdc())
                    .relay(serviceDetail.getRelay())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        return persistence(safety, jobId, jobCmd, jobDetailList,
                successJobId -> kvLogger.i(), error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
    }

    private String updateProfile(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        UpdateProfileCmdData updateProfileCmdData = (UpdateProfileCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("UpdateProfileInfo", JSON.toJSONString(updateProfileCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        ServiceDetailDTO serviceDetail = serverCacheInfoServiceBiz.getAgentInfo(updateProfileCmdData.getHostId());

        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (UpdateProfileCmdData.ContainerProfileInfo updateProfile : updateProfileCmdData.getProfileInfoList()) {
            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("hostId", updateProfileCmdData.getHostId())
                    .fluentPut("cid", updateProfile.getCid())
                    .fluentPut("profile", updateProfile.getProfile());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + updateProfile.getCid())
                    .jobHost(updateProfileCmdData.getHostId())
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(0)
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .idc(serviceDetail.getIdc())
                    .relay(serviceDetail.getRelay())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        return persistence(safety, jobId, jobCmd, jobDetailList,
                successJobId -> kvLogger.i(), error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
    }

    private String ctrl(JobSubTypeEnum jobSubType, JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        CtrlContainerCmdData ctrlContainerCmdData = (CtrlContainerCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("UpdateProfileInfo", JSON.toJSONString(ctrlContainerCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        ServiceDetailDTO serviceDetail = serverCacheInfoServiceBiz.getAgentInfo(ctrlContainerCmdData.getHostId());

        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String cid : ctrlContainerCmdData.getCIds()) {
            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("hostId", ctrlContainerCmdData.getHostId())
                    .fluentPut("cid", cid)
                    .fluentPut("ctrl", jobSubType.getName());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + cid)
                    .jobHost(cid)
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(0)
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .idc(serviceDetail.getIdc())
                    .relay(serviceDetail.getRelay())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        return persistence(safety, jobId, jobCmd, jobDetailList,
                successJobId -> kvLogger.i(), error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
    }

    private String execCmd(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        ContainerExecCmdData containerExecCmdData = (ContainerExecCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("ContainerExecCmd", JSON.toJSONString(containerExecCmdData))
                .p(HostStackConstants.JOB_ID, jobId);


        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String cid : containerExecCmdData.getCIds()) {
            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("script", containerExecCmdData.getScript())
                    .fluentPut("cid", cid);

            ServiceDetailDTO serviceDetail = serverCacheInfoServiceBiz.getAgentInfo(cid);

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + cid)
                    .jobHost(cid)
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(0)
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .idc(serviceDetail.getIdc())
                    .relay(serviceDetail.getRelay())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        return persistence(safety, jobId, jobCmd, jobDetailList,
                successJobId -> kvLogger.i(), error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
    }

    private R<SendJobResult> sendCreate(JobInnerCmd<?> jobCmd) {
        String jobId = jobCmd.getJobId();
        CreateContainerCmdData createContainerCmdData;
        if (jobCmd.getJobData() instanceof CreateContainerCmdData) {
            createContainerCmdData = (CreateContainerCmdData) jobCmd.getJobData();
        } else {
            createContainerCmdData = ((JSONObject) jobCmd.getJobData()).toJavaObject(CreateContainerCmdData.class);
        }

        List<JobParams.ContainerProfileTarget> targetList = Lists.newArrayList();
        List<String> jobDetailIds = Lists.newArrayList();

        for (CreateContainerCmdData.ContainerProfileInfo containerProfileInfo : createContainerCmdData.getProfileInfoList()) {
            String jobDetailId = jobId + StringPool.DASH + containerProfileInfo.getCid();
            jobDetailIds.add(jobDetailId);

            targetList.add(JobParams.ContainerProfileTarget.newBuilder()
                    .setCid(containerProfileInfo.getCid())
                    .setProfile(containerProfileInfo.getProfile())
                    .setJobDetailId(jobDetailId)
                    .build());
        }
        JobParams.ContainerCreate jobParams = JobParams.ContainerCreate.newBuilder()
                .setHostId(createContainerCmdData.getHostId())
                .setVmType(createContainerCmdData.getVmType())
                .setImage(JobParams.ImageOfContainerCreate.newBuilder()
                        .setId(createContainerCmdData.getImage().getId())
                        .setUrl(createContainerCmdData.getImage().getUrl())
                        .setVer(createContainerCmdData.getImage().getVer())
                        .setMd5(createContainerCmdData.getImage().getMd5())
                        .setUser(createContainerCmdData.getImage().getUser())
                        .setPwd(createContainerCmdData.getImage().getPassword())
                        .setSourceType(createContainerCmdData.getImage().getSourceType())
                        .build())
                .setProfileTemplate(createContainerCmdData.getProfileTemplate())
                .addAllTarget(targetList)
                .build();
        R<?> sendR = sendJobToAgent(createContainerCmdData.getHostId(), jobCmd, jobParams.toByteString());
        return buildSendResult(sendR, jobId, jobDetailIds);
    }

    private R<SendJobResult> sendUpgrade(JobInnerCmd<?> jobCmd) {
        String jobId = jobCmd.getJobId();
        UpgradeContainerCmdData upgradeContainerCmdData;
        if (jobCmd.getJobData() instanceof UpgradeContainerCmdData) {
            upgradeContainerCmdData = (UpgradeContainerCmdData) jobCmd.getJobData();
        } else {
            upgradeContainerCmdData = ((JSONObject) jobCmd.getJobData()).toJavaObject(UpgradeContainerCmdData.class);
        }

        List<JobParams.ContainerTarget> targetList = Lists.newArrayList();
        List<String> jobDetailIds = Lists.newArrayList();

        for (String cid : upgradeContainerCmdData.getCIds()) {
            String jobDetailId = jobId + StringPool.DASH + cid;
            jobDetailIds.add(jobDetailId);

            targetList.add(JobParams.ContainerTarget.newBuilder()
                    .setCid(cid)
                    .setJobDetailId(jobDetailId)
                    .build());
        }
        JobParams.ContainerUpgrade jobParams = JobParams.ContainerUpgrade.newBuilder()
                .setHostId(upgradeContainerCmdData.getHostId())
                .setVmType(upgradeContainerCmdData.getVmType())
                .setImage(JobParams.ImageOfContainerCreate.newBuilder()
                        .setId(upgradeContainerCmdData.getImage().getId())
                        .setUrl(upgradeContainerCmdData.getImage().getUrl())
                        .setVer(upgradeContainerCmdData.getImage().getVer())
                        .setMd5(upgradeContainerCmdData.getImage().getMd5())
                        .setUser(upgradeContainerCmdData.getImage().getUser())
                        .setPwd(upgradeContainerCmdData.getImage().getPassword())
                        .setSourceType(upgradeContainerCmdData.getImage().getSourceType())
                        .build())
                .addAllTarget(targetList)
                .build();
        R<?> sendR = sendJobToAgent(upgradeContainerCmdData.getHostId(), jobCmd, jobParams.toByteString());
        return buildSendResult(sendR, jobId, jobDetailIds);
    }

    private R<SendJobResult> sendUpdateProfile(JobInnerCmd<?> jobCmd) {
        String jobId = jobCmd.getJobId();
        UpdateProfileCmdData updateProfileCmdData;
        if (jobCmd.getJobData() instanceof UpdateProfileCmdData) {
            updateProfileCmdData = (UpdateProfileCmdData) jobCmd.getJobData();
        } else {
            updateProfileCmdData = ((JSONObject) jobCmd.getJobData()).toJavaObject(UpdateProfileCmdData.class);
        }

        List<JobParams.ContainerProfileTarget> targetList = Lists.newArrayList();
        List<String> jobDetailIds = Lists.newArrayList();

        for (UpdateProfileCmdData.ContainerProfileInfo updateProfile : updateProfileCmdData.getProfileInfoList()) {
            String jobDetailId = jobId + StringPool.DASH + updateProfile.getCid();
            jobDetailIds.add(jobDetailId);

            targetList.add(JobParams.ContainerProfileTarget.newBuilder()
                    .setCid(updateProfile.getCid())
                    .setProfile(updateProfile.getProfile())
                    .setJobDetailId(jobDetailId)
                    .build());
        }
        JobParams.ContainerUpdateProfile jobParams = JobParams.ContainerUpdateProfile.newBuilder()
                .setHostId(updateProfileCmdData.getHostId())
                .addAllTarget(targetList)
                .build();
        R<?> sendR = sendJobToAgent(updateProfileCmdData.getHostId(), jobCmd, jobParams.toByteString());
        return buildSendResult(sendR, jobId, jobDetailIds);
    }

    private R<SendJobResult> sendCtrl(JobSubTypeEnum jobSubType, JobInnerCmd<?> jobCmd) {
        String jobId = jobCmd.getJobId();
        CtrlContainerCmdData ctrlContainerCmdData;
        if (jobCmd.getJobData() instanceof CtrlContainerCmdData) {
            ctrlContainerCmdData = (CtrlContainerCmdData) jobCmd.getJobData();
        } else {
            ctrlContainerCmdData = ((JSONObject) jobCmd.getJobData()).toJavaObject(CtrlContainerCmdData.class);
        }

        List<JobParams.ContainerTarget> targetList = Lists.newArrayList();
        List<String> jobDetailIds = Lists.newArrayList();

        for (String cid : ctrlContainerCmdData.getCIds()) {
            String jobDetailId = jobId + StringPool.DASH + cid;
            jobDetailIds.add(jobDetailId);

            targetList.add(JobParams.ContainerTarget.newBuilder()
                    .setCid(cid)
                    .setJobDetailId(jobDetailId)
                    .build());
        }
        JobParams.ContainerCtrl jobParams = JobParams.ContainerCtrl.newBuilder()
                .setHostId(ctrlContainerCmdData.getHostId())
                .setCtrl(jobSubType.getName())
                .addAllTarget(targetList)
                .build();
        R<?> sendR = sendJobToAgent(ctrlContainerCmdData.getHostId(), jobCmd, jobParams.toByteString());
        return buildSendResult(sendR, jobId, jobDetailIds);
    }

    private R<SendJobResult> sendExecCmd(JobInnerCmd<?> jobCmd) {
        String jobId = jobCmd.getJobId();
        ContainerExecCmdData containerExecCmdData;
        if (jobCmd.getJobData() instanceof ContainerExecCmdData) {
            containerExecCmdData = (ContainerExecCmdData) jobCmd.getJobData();
        } else {
            containerExecCmdData = ((JSONObject) jobCmd.getJobData()).toJavaObject(ContainerExecCmdData.class);
        }

        // build default result
        SendJobResult sendJobResult = buildDefaultSendResult(jobId, containerExecCmdData.getCIds().size());
        // group hostId and service rel
        Map<String, List<String>> agentServiceGroupMap = groupAgentIdForService(containerExecCmdData.getCIds());
        // send job to edge by service
        for (String serviceId : agentServiceGroupMap.keySet()) {
            List<JobParams.ContainerTarget> targetList = Lists.newArrayList();
            for (String cid : agentServiceGroupMap.get(serviceId)) {
                String jobDetailId = jobId + StringPool.DASH + cid;

                targetList.add(JobParams.ContainerTarget.newBuilder()
                        .setCid(cid)
                        .setJobDetailId(jobDetailId)
                        .build());
            }
            JobParams.ContainerExecCmd jobParams = JobParams.ContainerExecCmd.newBuilder()
                    .setScript(containerExecCmdData.getScript())
                    .addAllTarget(targetList)
                    .build();
            List<String> jobDetailIds = targetList.stream().map(JobParams.ContainerTarget::getJobDetailId).toList();
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

    private void processCreateJobResult(JobReportMessage reportMessage) {
        JobStatusEnum jobStatus = JobStatusEnum.fromString(reportMessage.getStatus());
        if (jobStatus == JobStatusEnum.PROCESSING) {
            jobProcessService.updateDetailProgress(reportMessage.getJobId(), reportMessage.getJobDetailId(), reportMessage.getProgress());
        } else {
            // job success, to create container info
            if (jobStatus == JobStatusEnum.SUCCESS) {
                JobDetail jobDetail = jobProcessService.getJobDetail(reportMessage.getJobId(), reportMessage.getJobDetailId());
                String jobParamsStr = jobDetail.getJobParams();
                JSONObject jobParams = JSON.parseObject(jobParamsStr);
                JSONObject imageInfo = jobParams.getJSONObject("image");
                String hostId = jobParams.getString("hostId");
                String profile = jobParams.getString("profile");
                Long profileTemplateId = jobParams.getLong("profileTemplateId");
                String cid = jobDetail.getJobHost();
                String imageId = imageInfo.getString("id");
                String imageVer = imageInfo.getString("ver");
                String imageType = imageInfo.getString("type");

                SpringContextHolder.getBean(ContainerService.class).createContainer(reportMessage.getTraceId(), hostId, cid, profile,
                        imageId, imageVer, imageType, profileTemplateId, reportMessage.getTid());
            }
            // complete job
            jobProcessService.completeJob(jobStatus, reportMessage.getJobId(), reportMessage.getJobDetailId(), buildJobResult(reportMessage), null);
        }
    }

    private void processUpgradeJobResult(JobReportMessage reportMessage) {
        JobStatusEnum jobStatus = JobStatusEnum.fromString(reportMessage.getStatus());
        if (jobStatus == JobStatusEnum.PROCESSING) {
            jobProcessService.updateDetailProgress(reportMessage.getJobId(), reportMessage.getJobDetailId(), reportMessage.getProgress());
        } else {
            // job success, to update container image info
            if (jobStatus == JobStatusEnum.SUCCESS) {
                JobDetail jobDetail = jobProcessService.getJobDetail(reportMessage.getJobId(), reportMessage.getJobDetailId());
                String jobParamsStr = jobDetail.getJobParams();
                JSONObject jobParams = JSON.parseObject(jobParamsStr);
                JSONObject imageInfo = jobParams.getJSONObject("image");
                String cid = jobDetail.getJobHost();
                String imageId = imageInfo.getString("id");
                String imageVer = imageInfo.getString("ver");

                SpringContextHolder.getBean(ContainerService.class).upgradeContainer(reportMessage.getTraceId(), cid, imageId, imageVer);
            }
            // complete job
            jobProcessService.completeJob(jobStatus, reportMessage.getJobId(), reportMessage.getJobDetailId(), buildJobResult(reportMessage), null);
        }
    }

    private void processUpdateProfileJobResult(JobReportMessage reportMessage) {
        // TODO
    }

    private void processCtrlJobResult(JobReportMessage reportMessage) {
        JobStatusEnum jobStatus = JobStatusEnum.fromString(reportMessage.getStatus());
        if (jobStatus == JobStatusEnum.PROCESSING) {
            jobProcessService.updateDetailProgress(reportMessage.getJobId(), reportMessage.getJobDetailId(), reportMessage.getProgress());
        } else {
            // complete job
            jobProcessService.completeJob(jobStatus, reportMessage.getJobId(), reportMessage.getJobDetailId(), buildJobResult(reportMessage), null);
        }
    }

    private void processDropJobResult(JobReportMessage reportMessage) {
        JobDetail jobDetail = jobProcessService.getJobDetail(reportMessage.getJobId(), reportMessage.getJobDetailId());
        SpringContextHolder.getBean(ContainerService.class).releaseContainer(reportMessage.getTraceId(), jobDetail.getJobHost());
    }

    private void processExecCmdJobResult(JobReportMessage reportMessage) {
        JobStatusEnum jobStatus = JobStatusEnum.fromString(reportMessage.getStatus());
        if (jobStatus == JobStatusEnum.PROCESSING) {
            jobProcessService.updateDetailProgress(reportMessage.getJobId(), reportMessage.getJobDetailId(), reportMessage.getProgress());
        } else {
            // complete job
            jobProcessService.completeJob(jobStatus, reportMessage.getJobId(), reportMessage.getJobDetailId(), buildJobResult(reportMessage), null);
        }
    }
}
