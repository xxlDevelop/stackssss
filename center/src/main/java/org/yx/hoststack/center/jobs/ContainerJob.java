package org.yx.hoststack.center.jobs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.enums.JobStatusEnum;
import org.yx.hoststack.center.common.enums.JobSubTypeEnum;
import org.yx.hoststack.center.entity.JobDetail;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.jobs.cmd.container.*;
import org.yx.hoststack.center.service.JobDetailService;
import org.yx.hoststack.center.service.JobInfoService;
import org.yx.hoststack.center.service.biz.ServerCacheInfoServiceBiz;
import org.yx.hoststack.center.service.center.CenterService;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.protocol.ws.server.JobParams;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.StringPool;

import java.sql.Timestamp;
import java.util.List;
import java.util.function.Consumer;

@Service("container")
public class ContainerJob extends BaseJob implements IJob {
    public ContainerJob(JobInfoService jobInfoService, JobDetailService jobDetailService,
                        CenterService centerService, ServerCacheInfoServiceBiz serverCacheInfoServiceBiz,
                        TransactionTemplate transactionTemplate) {
        super(jobInfoService, jobDetailService, centerService, serverCacheInfoServiceBiz, transactionTemplate);
    }

    @Override
    public String doJob(JobInnerCmd<?> jobCmd) {
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
    public String safetyDoJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case CREATE -> create(jobCmd, true);
            case UPGRADE -> upgrade(jobCmd, true);
            case UPDATE_PROFILE -> updateProfile(jobCmd, true);
            case START, SHUTDOWN, REBOOT, DROP -> ctrl(jobCmd.getJobSubType(), jobCmd, true);
            case EXEC_CMD -> execCmd(jobCmd, true);
            default -> "";
        };
    }

    private String create(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        CreateContainerCmdData createContainerCmdData = (CreateContainerCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("CreateContainerInfo", JSON.toJSONString(createContainerCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JobParams.ContainerProfileTarget> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (CreateContainerCmdData.ContainerProfileInfo containerProfileInfo : createContainerCmdData.getProfileInfoList()) {
            targetList.add(JobParams.ContainerProfileTarget.newBuilder()
                    .setCid(containerProfileInfo.getCid())
                    .setProfile(containerProfileInfo.getProfile())
                    .setJobDetailId(jobId + StringPool.DASH + containerProfileInfo.getCid())
                    .build());

            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("hostId", createContainerCmdData.getHostId())
                    .fluentPut("vmType", createContainerCmdData.getVmType())
                    .fluentPut("image", createContainerCmdData.getImage())
                    .fluentPut("profileTemplate", createContainerCmdData.getProfileTemplate())
                    .fluentPut("cid", containerProfileInfo.getCid())
                    .fluentPut("profile", containerProfileInfo.getProfile())
                    .fluentPut("hostId", createContainerCmdData.getHostId());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + containerProfileInfo.getCid())
                    .jobHost(createContainerCmdData.getHostId())
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(RandomUtils.insecure().randomInt(1, 20))
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
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
        return persistence(safety, jobId, jobCmd, jobDetailList,
                successJobId -> {
                    kvLogger.i();
                    sendJobToAgent(jobCmd, createContainerCmdData.getHostId(), jobParams.toByteString());
                }, error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
    }

    private String upgrade(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        UpgradeContainerCmdData upgradeContainerCmdData = (UpgradeContainerCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("UpgradeContainerInfo", JSON.toJSONString(upgradeContainerCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JobParams.ContainerTarget> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String cid : upgradeContainerCmdData.getCIds()) {
            targetList.add(JobParams.ContainerTarget.newBuilder()
                    .setCid(cid)
                    .setJobDetailId(jobId + StringPool.DASH + cid)
                    .build());

            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("hostId", upgradeContainerCmdData.getHostId())
                    .fluentPut("vmType", upgradeContainerCmdData.getVmType())
                    .fluentPut("image", upgradeContainerCmdData.getImage())
                    .fluentPut("cid", cid)
                    .fluentPut("hostId", upgradeContainerCmdData.getHostId());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + cid)
                    .jobHost(upgradeContainerCmdData.getHostId())
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(RandomUtils.insecure().randomInt(1, 20))
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
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
        return persistence(safety, jobId, jobCmd, jobDetailList,
                successJobId -> {
                    kvLogger.i();
                    sendJobToAgent(jobCmd, upgradeContainerCmdData.getHostId(), jobParams.toByteString());
                }, error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
    }

    private String updateProfile(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        UpdateProfileCmdData updateProfileCmdData = (UpdateProfileCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("UpdateProfileInfo", JSON.toJSONString(updateProfileCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JobParams.ContainerProfileTarget> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (UpdateProfileCmdData.ContainerProfileInfo updateProfile : updateProfileCmdData.getProfileInfoList()) {
            targetList.add(JobParams.ContainerProfileTarget.newBuilder()
                    .setCid(updateProfile.getCid())
                    .setProfile(updateProfile.getProfile())
                    .setJobDetailId(jobId + StringPool.DASH + updateProfile.getCid())
                    .build());

            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("hostId", updateProfileCmdData.getHostId())
                    .fluentPut("cid", updateProfile.getCid())
                    .fluentPut("profile", updateProfile.getProfile());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + updateProfile.getCid())
                    .jobHost(updateProfileCmdData.getHostId())
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(RandomUtils.insecure().randomInt(1, 20))
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        JobParams.ContainerUpdateProfile jobParams = JobParams.ContainerUpdateProfile.newBuilder()
                .setHostId(updateProfileCmdData.getHostId())
                .addAllTarget(targetList)
                .build();
        return persistence(safety, jobId, jobCmd, jobDetailList,
                successJobId -> {
                    kvLogger.i();
                    sendJobToAgent(jobCmd, updateProfileCmdData.getHostId(), jobParams.toByteString());
                }, error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
    }

    private String ctrl(JobSubTypeEnum jobSubType, JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        CtrlContainerCmdData ctrlContainerCmdData = (CtrlContainerCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("UpdateProfileInfo", JSON.toJSONString(ctrlContainerCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JobParams.ContainerTarget> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String cid : ctrlContainerCmdData.getCIds()) {
            targetList.add(JobParams.ContainerTarget.newBuilder()
                    .setCid(cid)
                    .setJobDetailId(jobId + StringPool.DASH + cid)
                    .build());

            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("hostId", ctrlContainerCmdData.getHostId())
                    .fluentPut("cid", cid)
                    .fluentPut("ctrl", jobSubType.getName());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + cid)
                    .jobHost(ctrlContainerCmdData.getHostId())
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(RandomUtils.insecure().randomInt(1, 20))
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        JobParams.ContainerCtrl jobParams = JobParams.ContainerCtrl.newBuilder()
                .setHostId(ctrlContainerCmdData.getHostId())
                .setCtrl(jobSubType.getName())
                .addAllTarget(targetList)
                .build();
        return persistence(safety, jobId, jobCmd, jobDetailList,
                successJobId -> {
                    kvLogger.i();
                    sendJobToAgent(jobCmd, ctrlContainerCmdData.getHostId(), jobParams.toByteString());
                }, error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
    }

    private String execCmd(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        ContainerExecCmdData containerExecCmdData = (ContainerExecCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("ContainerExecCmd", JSON.toJSONString(containerExecCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JobParams.ContainerTarget> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String cid : containerExecCmdData.getCIds()) {
            targetList.add(JobParams.ContainerTarget.newBuilder()
                    .setCid(cid)
                    .setJobDetailId(jobId + StringPool.DASH + cid)
                    .build());

            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("script", containerExecCmdData.getScript())
                    .fluentPut("cid", cid);

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + cid)
                    .jobHost(cid)
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(RandomUtils.insecure().randomInt(1, 20))
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        JobParams.ContainerExecCmd jobParams = JobParams.ContainerExecCmd.newBuilder()
                .setScript(containerExecCmdData.getScript())
                .addAllTarget(targetList)
                .build();
        return persistence(safety, jobId, jobCmd, jobDetailList,
                successJobId -> {
                    kvLogger.i();
                    sendJobToAgent(jobCmd, containerExecCmdData.getCIds(), jobParams.toByteString());
                }, error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
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
}
