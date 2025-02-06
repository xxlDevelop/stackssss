package org.yx.hoststack.center.jobs;

import cn.hutool.log.Log;
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
import org.yx.hoststack.center.entity.JobDetail;
import org.yx.hoststack.center.entity.Volume;
import org.yx.hoststack.center.entity.VolumeMountRel;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.jobs.cmd.volume.*;
import org.yx.hoststack.center.queue.message.JobReportMessage;
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
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

@Service("volume")
public class VolumeJob extends BaseJob implements IJob {

    @Override
    public String createJob(JobInnerCmd<?> jobCmd) {
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
    public String safetyCreateJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case CREATE -> create(jobCmd, true);
            case DELETE -> delete(jobCmd, true);
            case MOUNT -> mount(jobCmd, true);
            case UNMOUNT -> unMount(jobCmd, true);
            case UPGRADE -> upgrade(jobCmd, true);
            default -> "";
        };
    }

    @Override
    public R<SendJobResult> sendJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case CREATE -> sendCreate(jobCmd);
            case DELETE -> sendDelete(jobCmd);
            case MOUNT -> sendMount(jobCmd);
            case UNMOUNT -> sendUnMount(jobCmd);
            case UPGRADE -> sendUpgrade(jobCmd);
            default -> R.failed(SysCode.x00000700.getValue(), SysCode.x00000700.getMsg());
        };
    }

    @Override
    public void processJobReportResult(JobReportMessage reportMessage) {
        switch (JobSubTypeEnum.fromString(reportMessage.getJobSubType())) {
            case CREATE:
                processCreateJobResult(reportMessage);
                break;
            case DELETE:
                processDeleteJobResult(reportMessage);
                break;
            case MOUNT:
                processMountJobResult(reportMessage);
                break;
            case UNMOUNT:
                processUnmountJobResult(reportMessage);
                break;
            case UPGRADE:
                processUpgradeJobResult(reportMessage);
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

    private String create(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        CreateVolumeCmdData createVolumeCmdData = (CreateVolumeCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("CreateVolumeInfo", JSONObject.toJSONString(createVolumeCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        ServiceDetailDTO serviceDetail = new ServiceDetailDTO(); //serverCacheInfoServiceBiz.getAgentInfo(createVolumeCmdData.getHostId());

        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String volumeId : createVolumeCmdData.getVolumeId()) {
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
                    .jobProgress(0)
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .idc(serviceDetail.getIdc())
                    .relay(serviceDetail.getRelay())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        return persistence(safety, jobId, jobCmd, jobDetailList,
                successJobId -> kvLogger.i(), error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
    }

    private String delete(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        DeleteVolumeCmdData deleteVolumeCmdData = (DeleteVolumeCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("DeleteVolumeInfo", JSONObject.toJSONString(deleteVolumeCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        ServiceDetailDTO serviceDetail = serverCacheInfoServiceBiz.getAgentInfo(deleteVolumeCmdData.getHostId());

        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (String volumeId : deleteVolumeCmdData.getVolumeIds()) {
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
                    .jobProgress(0)
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .idc(serviceDetail.getIdc())
                    .relay(serviceDetail.getRelay())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        return persistence(safety, jobId, jobCmd, jobDetailList,
                successJobId -> kvLogger.i(), error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
    }

    private String mount(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        MountVolumeCmdData mountVolumeCmdData = (MountVolumeCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("MountVolumeInfo", JSONObject.toJSONString(mountVolumeCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        ServiceDetailDTO serviceDetail = serverCacheInfoServiceBiz.getAgentInfo(mountVolumeCmdData.getHostId());

        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (MountVolumeCmdData.MountVolumeInfo mountInfo : mountVolumeCmdData.getMountInfoList()) {
            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("volumeId", mountInfo.getVolumeId())
                    .fluentPut("baseVolumeId", mountInfo.getBaseVolumeId())
                    .fluentPut("cid", mountInfo.getCid())
                    .fluentPut("mountType", mountInfo.getMountType())
                    .fluentPut("volumeHost", mountVolumeCmdData.getHostId());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobHost(mountInfo.getCid())
                    .jobDetailId(jobId + StringPool.DASH + mountInfo.getCid())
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(0)
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .idc(serviceDetail.getIdc())
                    .relay(serviceDetail.getRelay())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
                    .jobParams(jobDetailParam.toJSONString())
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        return persistence(safety, jobId, jobCmd, jobDetailList,
                successJobId -> kvLogger.i(), error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
    }

    private String unMount(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        UnMountVolumeCmdData unMountVolumeCmdData = (UnMountVolumeCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("UnMountVolumeInfo", JSONObject.toJSONString(unMountVolumeCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        ServiceDetailDTO serviceDetail = serverCacheInfoServiceBiz.getAgentInfo(unMountVolumeCmdData.getHostId());

        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (UnMountVolumeCmdData.UnMountVolumeInfo unMountInfo : unMountVolumeCmdData.getUnMountInfoList()) {
            JSONObject jobDetailParam = new JSONObject()
                    .fluentPut("volumeId", unMountInfo.getVolumeId())
                    .fluentPut("cid", unMountInfo.getCid())
                    .fluentPut("mountType", unMountInfo.getMountType())
                    .fluentPut("volumeHost", unMountVolumeCmdData.getHostId());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobHost(unMountInfo.getCid())
                    .jobDetailId(jobId + StringPool.DASH + unMountInfo.getCid())
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(0)
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .idc(serviceDetail.getIdc())
                    .relay(serviceDetail.getRelay())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
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
        UpgradeVolumeCmdData upgradeVolumeCmdData = (UpgradeVolumeCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("UpgradeVolumeInfo", JSONObject.toJSONString(upgradeVolumeCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        ServiceDetailDTO serviceDetail = serverCacheInfoServiceBiz.getAgentInfo(upgradeVolumeCmdData.getHostId());

        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (UpgradeVolumeCmdData.UpgradeVolumeInfo upgradeInfo : upgradeVolumeCmdData.getUpgradeInfoList()) {
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
                    .jobProgress(0)
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .idc(serviceDetail.getIdc())
                    .relay(serviceDetail.getRelay())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
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
        CreateVolumeCmdData createVolumeCmdData;
        if (jobCmd.getJobData() instanceof CreateVolumeCmdData) {
            createVolumeCmdData = (CreateVolumeCmdData) jobCmd.getJobData();
        } else {
            createVolumeCmdData = ((JSONObject) jobCmd.getJobData()).toJavaObject(CreateVolumeCmdData.class);
        }

        List<JobParams.VolumeTarget> targetList = Lists.newArrayList();
        List<String> jobDetailIds = Lists.newArrayList();

        for (String volumeId : createVolumeCmdData.getVolumeId()) {
            String jobDetailId = jobId + StringPool.DASH + volumeId;
            jobDetailIds.add(jobDetailId);

            targetList.add(JobParams.VolumeTarget.newBuilder()
                    .setVolumeId(volumeId)
                    .setJobDetailId(jobDetailId)
                    .build());
        }
        JobParams.VolumeCreate jobParams = JobParams.VolumeCreate.newBuilder()
                .setVolumeSize(createVolumeCmdData.getVolumeSize())
                .setVolumeType(createVolumeCmdData.getVolumeType())
                .setDiskType(createVolumeCmdData.getDiskType())
                .setSourceUrl(createVolumeCmdData.getSourceUrl())
                .setMd5(createVolumeCmdData.getMd5())
                .setHostId(createVolumeCmdData.getHostId())
                .setSnapshotName(createVolumeCmdData.getSnapshotName())
                .addAllTarget(targetList)
                .build();
        R<?> sendR = sendJobToAgent(createVolumeCmdData.getHostId(), jobCmd, jobParams.toByteString());
        return buildSendResult(sendR, jobId, jobDetailIds);
    }

    private R<SendJobResult> sendDelete(JobInnerCmd<?> jobCmd) {
        String jobId = jobCmd.getJobId();
        DeleteVolumeCmdData deleteVolumeCmdData;
        if (jobCmd.getJobData() instanceof DeleteVolumeCmdData) {
            deleteVolumeCmdData = (DeleteVolumeCmdData) jobCmd.getJobData();
        } else {
            deleteVolumeCmdData = ((JSONObject) jobCmd.getJobData()).toJavaObject(DeleteVolumeCmdData.class);
        }

        List<JobParams.VolumeTarget> targetList = Lists.newArrayList();
        List<String> jobDetailIds = Lists.newArrayList();

        for (String volumeId : deleteVolumeCmdData.getVolumeIds()) {
            String jobDetailId = jobId + StringPool.DASH + volumeId;
            jobDetailIds.add(jobDetailId);

            targetList.add(JobParams.VolumeTarget.newBuilder()
                    .setVolumeId(volumeId)
                    .setJobDetailId(jobDetailId)
                    .build());
        }
        JobParams.VolumeDelete jobParams = JobParams.VolumeDelete.newBuilder()
                .setHostId(deleteVolumeCmdData.getHostId())
                .setDiskType(deleteVolumeCmdData.getDiskType())
                .setVolumeType(deleteVolumeCmdData.getVolumeType())
                .addAllTarget(targetList)
                .build();
        R<?> sendR = sendJobToAgent(deleteVolumeCmdData.getHostId(), jobCmd, jobParams.toByteString());
        return buildSendResult(sendR, jobId, jobDetailIds);
    }

    private R<SendJobResult> sendMount(JobInnerCmd<?> jobCmd) {
        String jobId = jobCmd.getJobId();
        MountVolumeCmdData mountVolumeCmdData;
        if (jobCmd.getJobData() instanceof MountVolumeCmdData) {
            mountVolumeCmdData = (MountVolumeCmdData) jobCmd.getJobData();
        } else {
            mountVolumeCmdData = ((JSONObject) jobCmd.getJobData()).toJavaObject(MountVolumeCmdData.class);
        }

        List<JobParams.MountInfo> targetList = Lists.newArrayList();
        List<String> jobDetailIds = Lists.newArrayList();

        for (MountVolumeCmdData.MountVolumeInfo mountInfo : mountVolumeCmdData.getMountInfoList()) {
            String jobDetailId = jobId + StringPool.DASH + mountInfo.getCid();
            jobDetailIds.add(jobDetailId);

            targetList.add(JobParams.MountInfo.newBuilder()
                    .setVolumeId(mountInfo.getVolumeId())
                    .setBaseVolumeId(mountInfo.getBaseVolumeId())
                    .setCid(mountInfo.getCid())
                    .setMountType(mountInfo.getMountType())
                    .setJobDetailId(jobDetailId)
                    .build());
        }
        JobParams.VolumeMount jobParams = JobParams.VolumeMount.newBuilder()
                .setHostId(mountVolumeCmdData.getHostId())
                .addAllTarget(targetList)
                .build();
        R<?> sendR = sendJobToAgent(mountVolumeCmdData.getHostId(), jobCmd, jobParams.toByteString());
        return buildSendResult(sendR, jobId, jobDetailIds);
    }

    private R<SendJobResult> sendUnMount(JobInnerCmd<?> jobCmd) {
        String jobId = jobCmd.getJobId();
        UnMountVolumeCmdData unMountVolumeCmdData;
        if (jobCmd.getJobData() instanceof UnMountVolumeCmdData) {
            unMountVolumeCmdData = (UnMountVolumeCmdData) jobCmd.getJobData();
        } else {
            unMountVolumeCmdData = ((JSONObject) jobCmd.getJobData()).toJavaObject(UnMountVolumeCmdData.class);
        }

        List<JobParams.UnMountInfo> targetList = Lists.newArrayList();
        List<String> jobDetailIds = Lists.newArrayList();

        for (UnMountVolumeCmdData.UnMountVolumeInfo unMountInfo : unMountVolumeCmdData.getUnMountInfoList()) {
            String jobDetailId = jobId + StringPool.DASH + unMountInfo.getCid();
            jobDetailIds.add(jobDetailId);

            targetList.add(JobParams.UnMountInfo.newBuilder()
                    .setVolumeId(unMountInfo.getVolumeId())
                    .setCid(unMountInfo.getCid())
                    .setMountType(unMountInfo.getMountType())
                    .setJobDetailId(jobDetailId)
                    .build());
        }
        JobParams.VolumeUnMount jobParams = JobParams.VolumeUnMount.newBuilder()
                .setHostId(unMountVolumeCmdData.getHostId())
                .addAllTarget(targetList)
                .build();
        R<?> sendR = sendJobToAgent(unMountVolumeCmdData.getHostId(), jobCmd, jobParams.toByteString());
        return buildSendResult(sendR, jobId, jobDetailIds);
    }

    private R<SendJobResult> sendUpgrade(JobInnerCmd<?> jobCmd) {
        String jobId = jobCmd.getJobId();
        UpgradeVolumeCmdData upgradeVolumeCmdData;
        if (jobCmd.getJobData() instanceof UpgradeVolumeCmdData) {
            upgradeVolumeCmdData = (UpgradeVolumeCmdData) jobCmd.getJobData();
        } else {
            upgradeVolumeCmdData = ((JSONObject) jobCmd.getJobData()).toJavaObject(UpgradeVolumeCmdData.class);
        }

        List<JobParams.VolumeUpgradeDetail> targetList = Lists.newArrayList();
        List<String> jobDetailIds = Lists.newArrayList();

        for (UpgradeVolumeCmdData.UpgradeVolumeInfo upgradeInfo : upgradeVolumeCmdData.getUpgradeInfoList()) {
            String jobDetailId = jobId + StringPool.DASH + upgradeInfo.getOriginVolumeId();
            jobDetailIds.add(jobDetailId);

            targetList.add(JobParams.VolumeUpgradeDetail.newBuilder()
                    .setOriginVolumeId(upgradeInfo.getOriginVolumeId())
                    .setNewVolumeId(upgradeInfo.getNewVolumeId())
                    .setKeepOrigin(upgradeInfo.isKeepOrigin())
                    .setJobDetailId(jobDetailId)
                    .build());
        }
        JobParams.VolumeUpgrade jobParams = JobParams.VolumeUpgrade.newBuilder()
                .setHostId(upgradeVolumeCmdData.getHostId())
                .setSourceUrl(upgradeVolumeCmdData.getSourceUrl())
                .setMd5(upgradeVolumeCmdData.getMd5())
                .addAllTarget(targetList)
                .build();
        R<?> sendR = sendJobToAgent(upgradeVolumeCmdData.getHostId(), jobCmd, jobParams.toByteString());
        return buildSendResult(sendR, jobId, jobDetailIds);
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
            if (jobStatus == JobStatusEnum.FAIL || jobStatus == JobStatusEnum.SUCCESS) {
                // detail job success, to create volume info
                if (jobStatus == JobStatusEnum.SUCCESS) {
                    JobDetail jobDetail = jobProcessService.getJobDetail(reportMessage.getJobId(), reportMessage.getJobDetailId());
                    // jobParams: {"volumeType":"user","volumeId":"volumeId01","hostId":"hostId01","diskType":"local","volumeSize":100,"hostId":"hostId","md5":"md5","sourceUrl":"sourceUrl","snapshotName":"snapshotName"}
                    String jobParams = jobDetail.getJobParams();
                    JSONObject volumeObject = JSONObject.parseObject(jobParams);
                    String volumeType = volumeObject.getString("volumeType");
                    String volumeId = volumeObject.getString("volumeId");
                    String hostId = volumeObject.getString("hostId");
                    String diskType = volumeObject.getString("diskType");
                    Long volumeSize = volumeObject.getLong("volumeSize");
                    String md5 = volumeObject.getString("md5");
                    String snapshotName = volumeObject.getString("snapshotName");
                    String sourceUrl = volumeObject.getString("sourceUrl");
                    VolumeService volumeService = SpringContextHolder.getBean(VolumeService.class);
                    volumeService.insert(Volume.builder()
                            .volumeType(volumeType)
                            .volumeId(volumeId)
                            .hostId(hostId)
                            .diskType(diskType)
                            .md5(md5)
                            .volumeSize(volumeSize)
                            .snapshotName(snapshotName)
                            .downloadUrl(sourceUrl)
                            .build());
                    KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, CenterEvent.PROCESS_JOB_NOTIFY)
                            .p(LogFieldConstants.ACTION, CenterEvent.Action.CREATE_VOLUME)
                            .p(LogFieldConstants.TID, reportMessage.getTid())
                            .p(LogFieldConstants.TRACE_ID, reportMessage.getTraceId())
                            .p("CreateVolumeInfo", jobParams)
                            .p(HostStackConstants.JOB_ID, reportMessage.getJobId())
                            .p(HostStackConstants.JOB_DETAIL_ID, reportMessage.getJobDetailId())
                            .i();
                }
                // complete job
                jobProcessService.completeJob(jobStatus, reportMessage.getJobId(), reportMessage.getJobDetailId(), buildJobResult(reportMessage), null);
            }
        }
    }

    private void processDeleteJobResult(JobReportMessage reportMessage) {
        JobStatusEnum jobStatus = JobStatusEnum.fromString(reportMessage.getStatus());
        if (jobStatus == JobStatusEnum.PROCESSING) {
            jobProcessService.updateDetailProgress(reportMessage.getJobId(), reportMessage.getJobDetailId(), reportMessage.getProgress());
        } else {
            if (jobStatus == JobStatusEnum.FAIL || jobStatus == JobStatusEnum.SUCCESS) {
                // detail job success, to delete volume info
                if (jobStatus == JobStatusEnum.SUCCESS) {
                    JobDetail jobDetail = jobProcessService.getJobDetail(reportMessage.getJobId(), reportMessage.getJobDetailId());
                    // jobParams: {"volumeType":"user","volumeId":"volumeId01","hostId":"hostId01","diskType":"local"}
                    String jobParams = jobDetail.getJobParams();
                    JSONObject volumeObject = JSONObject.parseObject(jobParams);
                    String volumeId = volumeObject.getString("volumeId");
                    String hostId = volumeObject.getString("hostId");
                    VolumeService volumeService = SpringContextHolder.getBean(VolumeService.class);
                    volumeService.remove(Wrappers.lambdaQuery(Volume.class)
                            .eq(Volume::getVolumeId, volumeId)
                            .eq(Volume::getHostId, hostId)
                    );
                    KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, CenterEvent.PROCESS_JOB_NOTIFY)
                            .p(LogFieldConstants.ACTION, CenterEvent.Action.CREATE_VOLUME)
                            .p(LogFieldConstants.TID, reportMessage.getTid())
                            .p(LogFieldConstants.TRACE_ID, reportMessage.getTraceId())
                            .p("DeleteVolumeInfo", jobParams)
                            .p(HostStackConstants.JOB_ID, reportMessage.getJobId())
                            .p(HostStackConstants.JOB_DETAIL_ID, reportMessage.getJobDetailId())
                            .i();
                }
                // complete job
                jobProcessService.completeJob(jobStatus, reportMessage.getJobId(), reportMessage.getJobDetailId(), buildJobResult(reportMessage), null);
            }
        }
    }

    private void processMountJobResult(JobReportMessage reportMessage) {
        JobStatusEnum jobStatus = JobStatusEnum.fromString(reportMessage.getStatus());
        if (jobStatus == JobStatusEnum.PROCESSING) {
            jobProcessService.updateDetailProgress(reportMessage.getJobId(), reportMessage.getJobDetailId(), reportMessage.getProgress());
        } else {
            if (jobStatus == JobStatusEnum.FAIL || jobStatus == JobStatusEnum.SUCCESS) {
                // detail job success, to create mount info
                if (jobStatus == JobStatusEnum.SUCCESS) {
                    JobDetail jobDetail = jobProcessService.getJobDetail(reportMessage.getJobId(), reportMessage.getJobDetailId());
                    // jobParams: {"volumeId":"volumeId","baseVolumeId":"baseVolumeId","cid":"cid","mountType":"local","volumeHost":"volumeHost"}
                    String jobParams = jobDetail.getJobParams();
                    JSONObject volumeObject = JSONObject.parseObject(jobParams);
                    String volumeId = volumeObject.getString("volumeId");
                    String baseVolumeId = volumeObject.getString("baseVolumeId");
                    String cid = volumeObject.getString("cid");
                    String mountType = volumeObject.getString("mountType");
                    String volumeHost = volumeObject.getString("volumeHost");
                    VolumeMountRelService volumeMountRelService = SpringContextHolder.getBean(VolumeMountRelService.class);
                    // user volume
                    volumeMountRelService.insert(VolumeMountRel.builder()
                            .volumeId(volumeId)
                            .volumeType("user")
                            .mountContainerId(cid)
                            .volumeHost(volumeHost)
                            .mountType(mountType)
                            .mountAt(new Date(System.currentTimeMillis()))
                            .build());
                    // base volume
                    volumeMountRelService.insert(VolumeMountRel.builder()
                            .volumeId(baseVolumeId)
                            .volumeType("base")
                            .mountContainerId(cid)
                            .volumeHost(volumeHost)
                            .mountType(mountType)
                            .mountAt(new Date(System.currentTimeMillis()))
                            .build());
                    KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, CenterEvent.PROCESS_JOB_NOTIFY)
                            .p(LogFieldConstants.ACTION, CenterEvent.Action.MOUNT_VOLUME)
                            .p(LogFieldConstants.TID, reportMessage.getTid())
                            .p(LogFieldConstants.TRACE_ID, reportMessage.getTraceId())
                            .p("MountVolumeInfo", jobParams)
                            .p(HostStackConstants.JOB_ID, reportMessage.getJobId())
                            .p(HostStackConstants.JOB_DETAIL_ID, reportMessage.getJobDetailId())
                            .i();
                }
                // complete job
                jobProcessService.completeJob(jobStatus, reportMessage.getJobId(), reportMessage.getJobDetailId(), buildJobResult(reportMessage), null);
            }
        }
    }

    private void processUnmountJobResult(JobReportMessage reportMessage) {
        JobStatusEnum jobStatus = JobStatusEnum.fromString(reportMessage.getStatus());
        if (jobStatus == JobStatusEnum.PROCESSING) {
            jobProcessService.updateDetailProgress(reportMessage.getJobId(), reportMessage.getJobDetailId(), reportMessage.getProgress());
        } else {
            if (jobStatus == JobStatusEnum.FAIL || jobStatus == JobStatusEnum.SUCCESS) {
                // detail job success, to create mount info
                if (jobStatus == JobStatusEnum.SUCCESS) {
                    JobDetail jobDetail = jobProcessService.getJobDetail(reportMessage.getJobId(), reportMessage.getJobDetailId());
                    // jobParams: {"volumeId":"volumeId","cid":"cid","mountType":"local","volumeHost":"volumeHost"}
                    String jobParams = jobDetail.getJobParams();
                    JSONObject volumeObject = JSONObject.parseObject(jobParams);
                    String volumeId = volumeObject.getString("volumeId");
                    String cid = volumeObject.getString("cid");
                    String volumeHost = volumeObject.getString("volumeHost");
                    VolumeMountRelService volumeMountRelService = SpringContextHolder.getBean(VolumeMountRelService.class);
                    volumeMountRelService.remove(Wrappers.lambdaQuery(VolumeMountRel.class)
                            .eq(VolumeMountRel::getVolumeId, volumeId)
                            .eq(VolumeMountRel::getMountContainerId, cid)
                            .eq(VolumeMountRel::getVolumeHost, volumeHost));
                    KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, CenterEvent.PROCESS_JOB_NOTIFY)
                            .p(LogFieldConstants.ACTION, CenterEvent.Action.UN_MOUNT_VOLUME)
                            .p(LogFieldConstants.TID, reportMessage.getTid())
                            .p(LogFieldConstants.TRACE_ID, reportMessage.getTraceId())
                            .p("UnMountVolumeInfo", jobParams)
                            .p(HostStackConstants.JOB_ID, reportMessage.getJobId())
                            .p(HostStackConstants.JOB_DETAIL_ID, reportMessage.getJobDetailId())
                            .i();
                }
                // complete job
                jobProcessService.completeJob(jobStatus, reportMessage.getJobId(), reportMessage.getJobDetailId(), buildJobResult(reportMessage), null);
            }
        }
    }

    private void processUpgradeJobResult(JobReportMessage reportMessage) {
        // TODO
    }
}
