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
import org.yx.hoststack.common.HostStackConstants;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.StringPool;

import java.sql.Timestamp;
import java.util.List;

@Service("container")
public class ContainerJob extends BaseJob implements IJob {
    public ContainerJob(JobInfoService jobInfoService, JobDetailService jobDetailService,
                        TransactionTemplate transactionTemplate) {
        super(jobInfoService, jobDetailService, transactionTemplate);
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

        List<JSONObject> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (CreateContainerCmdData.ContainerProfileInfo containerProfileInfo : createContainerCmdData.getProfileInfoList()) {
            JSONObject target = new JSONObject()
                    .fluentPut("cid", containerProfileInfo.getCid())
                    .fluentPut("profile", containerProfileInfo.getProfile())
                    .fluentPut("jobDetailId", jobId + StringPool.DASH + containerProfileInfo.getCid());
            targetList.add(target);

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
        JSONObject jobParams = new JSONObject()
                .fluentPut("hostId", createContainerCmdData.getHostId())
                .fluentPut("vmType", createContainerCmdData.getVmType())
                .fluentPut("image", createContainerCmdData.getImage())
                .fluentPut("profileTemplate", createContainerCmdData.getProfileTemplate())
                .fluentPut("target", targetList);
        return persistence(safety, jobId, jobCmd, jobDetailList, kvLogger);
    }

    private String upgrade(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        UpgradeContainerCmdData upgradeContainerCmdData = (UpgradeContainerCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("UpgradeContainerInfo", JSON.toJSONString(upgradeContainerCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JSONObject> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String cid : upgradeContainerCmdData.getCIds()) {
            JSONObject target = new JSONObject()
                    .fluentPut("cid", cid)
                    .fluentPut("jobDetailId", jobId + StringPool.DASH + cid);
            targetList.add(target);

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
        JSONObject jobParams = new JSONObject()
                .fluentPut("hostId", upgradeContainerCmdData.getHostId())
                .fluentPut("vmType", upgradeContainerCmdData.getVmType())
                .fluentPut("image", upgradeContainerCmdData.getImage())
                .fluentPut("target", targetList);
        return persistence(safety, jobId, jobCmd, jobDetailList, kvLogger);
    }

    private String updateProfile(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        UpdateProfileCmdData updateProfileCmdData = (UpdateProfileCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("UpdateProfileInfo", JSON.toJSONString(updateProfileCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JSONObject> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (UpdateProfileCmdData.ContainerProfileInfo updateProfile : updateProfileCmdData.getProfileInfoList()) {
            JSONObject target = new JSONObject()
                    .fluentPut("cid", updateProfile.getCid())
                    .fluentPut("profile", updateProfile.getProfile())
                    .fluentPut("jobDetailId", jobId + StringPool.DASH + updateProfile.getCid());
            targetList.add(target);

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
        JSONObject jobParams = new JSONObject()
                .fluentPut("hostId", updateProfileCmdData.getHostId())
                .fluentPut("target", targetList);
        return persistence(safety, jobId, jobCmd, jobDetailList, kvLogger);
    }

    private String ctrl(JobSubTypeEnum jobSubType, JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        CtrlContainerCmdData ctrlContainerCmdData = (CtrlContainerCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("UpdateProfileInfo", JSON.toJSONString(ctrlContainerCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JSONObject> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String cid : ctrlContainerCmdData.getCIds()) {
            JSONObject target = new JSONObject()
                    .fluentPut("cid", cid)
                    .fluentPut("jobDetailId", jobId + StringPool.DASH + cid);
            targetList.add(target);

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
        JSONObject jobParams = new JSONObject()
                .fluentPut("hostId", ctrlContainerCmdData.getHostId())
                .fluentPut("ctrl", jobSubType.getName())
                .fluentPut("target", targetList);
        return persistence(safety, jobId, jobCmd, jobDetailList, kvLogger);
    }

    private String execCmd(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        ContainerExecCmdData containerExecCmdData = (ContainerExecCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("ContainerExecCmd", JSON.toJSONString(containerExecCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JSONObject> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String cid : containerExecCmdData.getCIds()) {
            JSONObject target = new JSONObject()
                    .fluentPut("cid", cid)
                    .fluentPut("jobDetailId", jobId + StringPool.DASH + cid);
            targetList.add(target);

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
        JSONObject jobParams = new JSONObject()
                .fluentPut("script", containerExecCmdData.getScript())
                .fluentPut("target", targetList);
        return persistence(safety, jobId, jobCmd, jobDetailList, kvLogger);
    }

    private String persistence(boolean safety, String jobId, JobInnerCmd<?> jobCmd, List<JobDetail> jobDetailList,
                               KvLogger kvLogger) {
        if (safety) {
            try {
                safetyPersistenceJob(jobId, jobCmd, null, "", jobDetailList);
                kvLogger.i();
                return jobId;
            } catch (Exception ex) {
                kvLogger.p(LogFieldConstants.ERR_MSG, ex.getMessage())
                        .e(ex);
                return "";
            }
        } else {
            persistenceJob(jobId, jobCmd, null, "", jobDetailList);
            kvLogger.i();
            return jobId;
        }
    }
}
