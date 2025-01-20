package org.yx.hoststack.center.jobs;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.enums.JobStatusEnum;
import org.yx.hoststack.center.entity.JobDetail;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.jobs.cmd.volume.*;
import org.yx.hoststack.center.service.JobDetailService;
import org.yx.hoststack.center.service.JobInfoService;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.StringPool;

import java.sql.Timestamp;
import java.util.List;

@Service("volume")
public class VolumeJob extends BaseJob implements IJob {

    public VolumeJob(JobInfoService jobInfoService, JobDetailService jobDetailService,
                     TransactionTemplate transactionTemplate) {
        super(jobInfoService, jobDetailService, transactionTemplate);
    }

    @Override
    public String doJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case CREATE -> create(jobCmd, false);
            case DELETE -> delete(jobCmd, false);
            case MOUNT -> mount(jobCmd, false);
            case UNMOUNT -> unMount(jobCmd, false);
            case UPGRADE -> upgrade(jobCmd, false);
            default -> "";
        };
    }

    @Override
    public String safetyDoJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case CREATE -> create(jobCmd, true);
            case DELETE -> delete(jobCmd, true);
            case MOUNT -> mount(jobCmd, true);
            case UNMOUNT -> unMount(jobCmd, true);
            case UPGRADE -> upgrade(jobCmd, true);
            default -> "";
        };
    }

    private String create(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        CreateVolumeCmdData createVolumeCmdData = (CreateVolumeCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("CreateVolumeInfo", JSONObject.toJSONString(createVolumeCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JSONObject> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String volumeId : createVolumeCmdData.getVolumeId()) {
            JSONObject target = new JSONObject()
                    .fluentPut("volumeId", volumeId)
                    .fluentPut("jobDetailId", jobId + StringPool.DASH + volumeId);
            targetList.add(target);

            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("volumeId", volumeId)
                    .fluentPut("volumeSize", createVolumeCmdData.getVolumeSize())
                    .fluentPut("volumeType", createVolumeCmdData.getVolumeType())
                    .fluentPut("diskType", createVolumeCmdData.getDiskType())
                    .fluentPut("sourceUrl", createVolumeCmdData.getSourceUrl())
                    .fluentPut("md5", createVolumeCmdData.getMd5())
                    .fluentPut("hostId", createVolumeCmdData.getHostId())
                    .fluentPut("snapshotName", createVolumeCmdData.getSnapshotName());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobHost(volumeId)
                    .jobDetailId(jobId + StringPool.DASH + volumeId)
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(RandomUtils.insecure().randomInt(1, 50))
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        JSONObject jobParams = new JSONObject()
                .fluentPut("volumeSize", createVolumeCmdData.getVolumeSize())
                .fluentPut("volumeType", createVolumeCmdData.getVolumeType())
                .fluentPut("diskType", createVolumeCmdData.getDiskType())
                .fluentPut("sourceUrl", createVolumeCmdData.getSourceUrl())
                .fluentPut("md5", createVolumeCmdData.getMd5())
                .fluentPut("hostId", createVolumeCmdData.getHostId())
                .fluentPut("snapshotName", createVolumeCmdData.getSnapshotName())
                .fluentPut("target", targetList);
        return persistence(safety, jobId, jobCmd, jobDetailList, kvLogger);
    }

    private String delete(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        DeleteVolumeCmdData deleteVolumeCmdData = (DeleteVolumeCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("DeleteVolumeInfo", JSONObject.toJSONString(deleteVolumeCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JSONObject> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String volumeId : deleteVolumeCmdData.getVolumeIds()) {
            JSONObject target = new JSONObject()
                    .fluentPut("volumeId", volumeId)
                    .fluentPut("jobDetailId", jobId + StringPool.DASH + volumeId);
            targetList.add(target);

            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("volumeId", volumeId)
                    .fluentPut("volumeType", deleteVolumeCmdData.getVolumeType())
                    .fluentPut("diskType", deleteVolumeCmdData.getDiskType())
                    .fluentPut("hostId", deleteVolumeCmdData.getHostId());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobHost(volumeId)
                    .jobDetailId(jobId + StringPool.DASH + volumeId)
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(RandomUtils.insecure().randomInt(1, 50))
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        JSONObject jobParams = new JSONObject()
                .fluentPut("volumeType", deleteVolumeCmdData.getVolumeType())
                .fluentPut("diskType", deleteVolumeCmdData.getDiskType())
                .fluentPut("hostId", deleteVolumeCmdData.getHostId())
                .fluentPut("target", targetList);
        return persistence(safety, jobId, jobCmd, jobDetailList, kvLogger);
    }

    private String mount(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        MountVolumeCmdData mountVolumeCmdData = (MountVolumeCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("MountVolumeInfo", JSONObject.toJSONString(mountVolumeCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JSONObject> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (MountVolumeCmdData.MountVolumeInfo mountInfo : mountVolumeCmdData.getMountInfoList()) {
            JSONObject target = new JSONObject()
                    .fluentPut("volumeId", mountInfo.getVolumeId())
                    .fluentPut("baseVolumeId", mountInfo.getBaseVolumeId())
                    .fluentPut("cid", mountInfo.getCid())
                    .fluentPut("mountType", mountInfo.getMountType())
                    .fluentPut("jobDetailId", jobId + StringPool.DASH + mountInfo.getCid());
            targetList.add(target);

            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("volumeId", mountInfo.getVolumeId())
                    .fluentPut("baseVolumeId", mountInfo.getBaseVolumeId())
                    .fluentPut("cid", mountInfo.getCid())
                    .fluentPut("mountType", mountInfo.getMountType())
                    .fluentPut("hostId", mountVolumeCmdData.getHostId());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobHost(mountInfo.getCid())
                    .jobDetailId(jobId + StringPool.DASH + mountInfo.getCid())
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(RandomUtils.insecure().randomInt(1, 50))
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        JSONObject jobParams = new JSONObject()
                .fluentPut("hostId", mountVolumeCmdData.getHostId())
                .fluentPut("target", targetList);
        return persistence(safety, jobId, jobCmd, jobDetailList, kvLogger);
    }

    private String unMount(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        UnMountVolumeCmdData unMountVolumeCmdData = (UnMountVolumeCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("UnMountVolumeInfo", JSONObject.toJSONString(unMountVolumeCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JSONObject> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (UnMountVolumeCmdData.UnMountVolumeInfo unMountInfo : unMountVolumeCmdData.getUnMountInfoList()) {
            JSONObject target = new JSONObject()
                    .fluentPut("volumeId", unMountInfo.getVolumeId())
                    .fluentPut("cid", unMountInfo.getCid())
                    .fluentPut("mountType", unMountInfo.getMountType())
                    .fluentPut("jobDetailId", jobId + StringPool.DASH + unMountInfo.getCid());
            targetList.add(target);

            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("volumeId", unMountInfo.getVolumeId())
                    .fluentPut("cid", unMountInfo.getCid())
                    .fluentPut("mountType", unMountInfo.getMountType())
                    .fluentPut("hostId", unMountVolumeCmdData.getHostId());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobHost(unMountInfo.getCid())
                    .jobDetailId(jobId + StringPool.DASH + unMountInfo.getCid())
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(RandomUtils.insecure().randomInt(1, 50))
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        JSONObject jobParams = new JSONObject()
                .fluentPut("hostId", unMountVolumeCmdData.getHostId())
                .fluentPut("target", targetList);
        return persistence(safety, jobId, jobCmd, jobDetailList, kvLogger);
    }

    private String upgrade(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        UpgradeVolumeCmdData upgradeVolumeCmdData = (UpgradeVolumeCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("UpgradeVolumeInfo", JSONObject.toJSONString(upgradeVolumeCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JSONObject> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (UpgradeVolumeCmdData.UpgradeVolumeInfo upgradeInfo : upgradeVolumeCmdData.getUpgradeInfoList()) {
            JSONObject target = new JSONObject()
                    .fluentPut("sourceUrl", upgradeVolumeCmdData.getSourceUrl())
                    .fluentPut("md5", upgradeVolumeCmdData.getMd5())
                    .fluentPut("originVolumeId", upgradeInfo.getOriginVolumeId())
                    .fluentPut("newVolumeId", upgradeInfo.getNewVolumeId())
                    .fluentPut("keepOrigin", upgradeInfo.isKeepOrigin())
                    .fluentPut("jobDetailId", jobId + StringPool.DASH + upgradeInfo.getOriginVolumeId());
            targetList.add(target);

            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("sourceUrl", upgradeVolumeCmdData.getSourceUrl())
                    .fluentPut("md5", upgradeVolumeCmdData.getMd5())
                    .fluentPut("originVolumeId", upgradeInfo.getOriginVolumeId())
                    .fluentPut("newVolumeId", upgradeInfo.getNewVolumeId())
                    .fluentPut("keepOrigin", upgradeInfo.isKeepOrigin())
                    .fluentPut("hostId", upgradeVolumeCmdData.getHostId());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobHost(upgradeInfo.getOriginVolumeId())
                    .jobDetailId(jobId + StringPool.DASH + upgradeInfo.getOriginVolumeId())
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(RandomUtils.insecure().randomInt(1, 50))
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        JSONObject jobParams = new JSONObject()
                .fluentPut("hostId", upgradeVolumeCmdData.getHostId())
                .fluentPut("target", targetList);
        return persistence(safety, jobId, jobCmd, jobDetailList, kvLogger);
    }

    private String persistence(boolean safety, String jobId, JobInnerCmd<?> jobCmd, List<JobDetail> jobDetailList, KvLogger kvLogger) {
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
