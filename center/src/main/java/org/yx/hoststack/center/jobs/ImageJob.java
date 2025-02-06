package org.yx.hoststack.center.jobs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.dto.ServiceDetailDTO;
import org.yx.hoststack.center.common.enums.JobStatusEnum;
import org.yx.hoststack.center.common.enums.JobSubTypeEnum;
import org.yx.hoststack.center.common.enums.SysCode;
import org.yx.hoststack.center.entity.ImageDownloadInf;
import org.yx.hoststack.center.entity.ImageInfo;
import org.yx.hoststack.center.entity.JobDetail;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.jobs.cmd.image.CreateImageCmdData;
import org.yx.hoststack.center.jobs.cmd.image.DeleteImageCmdData;
import org.yx.hoststack.center.queue.message.JobReportMessage;
import org.yx.hoststack.center.service.ImageDownloadInfService;
import org.yx.hoststack.center.service.ImageInfoService;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.protocol.ws.server.JobParams;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.SpringContextHolder;
import org.yx.lib.utils.util.StringPool;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service("image")
public class ImageJob extends BaseJob implements IJob {

    @Override
    public String createJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case CREATE -> create(jobCmd, false);
            case DELETE -> delete(jobCmd, false);
            default -> "";
        };
    }

    @Override
    public String safetyCreateJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case CREATE -> create(jobCmd, true);
            case DELETE -> delete(jobCmd, true);
            default -> "";
        };
    }

    @Override
    public R<SendJobResult> sendJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case CREATE -> sendCreate(jobCmd);
            case DELETE -> sendDelete(jobCmd);
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
            default:
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, CenterEvent.PROCESS_JOB_NOTIFY)
                        .p(LogFieldConstants.ACTION, CenterEvent.Action.JOB_NOTIFY)
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
        CreateImageCmdData createImageCmdData = (CreateImageCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
                .p(LogFieldConstants.ACTION, CenterEvent.Action.PERSISTENCE_TO_DB)
                .p(HostStackConstants.JOB_TYPE, jobCmd.getJobType().getName())
                .p(HostStackConstants.JOB_SUB_TYPE, jobCmd.getJobSubType().getName())
                .flat("CreateImageInfo", createImageCmdData)
                .p(HostStackConstants.JOB_ID, jobId);

        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (CreateImageCmdData.IdcInfo idcInfo : createImageCmdData.getIdcInfos()) {
            ServiceDetailDTO serviceDetail = serverCacheInfoServiceBiz.getNodeInfo(idcInfo.getIdc());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobHost(createImageCmdData.getImageId())
                    .jobDetailId(jobId + StringPool.DASH + createImageCmdData.getImageId())
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(0)
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .idc(serviceDetail.getIdc())
                    .relay(serviceDetail.getRelay())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
                    .jobParams(JSON.toJSONString(new JSONObject()
                            .fluentPut("imageId", createImageCmdData.getImageId())
                            .fluentPut("imageName", createImageCmdData.getImageName())
                            .fluentPut("imageVer", createImageCmdData.getImageVer())
                            .fluentPut("downloadUrl", createImageCmdData.getDownloadUrl())
                            .fluentPut("md5", createImageCmdData.getMd5())
                            .fluentPut("idc", idcInfo.getIdc())
                            .fluentPut("bucket", idcInfo.getBucket())
                    ))
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        return persistence(safety, jobId, jobCmd, jobDetailList, (JSONObject) JSONObject.toJSON(createImageCmdData),
                successJobId -> kvLogger.i(), error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
    }

    private String delete(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        DeleteImageCmdData deleteImageCmdData = (DeleteImageCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("DeleteImageInfo", JSON.toJSONString(deleteImageCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (DeleteImageCmdData.IdcInfo idcInfo : deleteImageCmdData.getIdcInfos()) {
            ServiceDetailDTO serviceDetail = serverCacheInfoServiceBiz.getNodeInfo(idcInfo.getIdc());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + deleteImageCmdData.getImageId())
                    .jobHost(deleteImageCmdData.getImageId())
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(0)
                    .zone(serviceDetail.getZone())
                    .region(serviceDetail.getRegion())
                    .idc(serviceDetail.getIdc())
                    .relay(serviceDetail.getRelay())
                    .idcSid(serviceDetail.getIdcSid())
                    .relaySid(serviceDetail.getRelaySid())
                    .jobParams(JSON.toJSONString(new JSONObject()
                            .fluentPut("imageId", deleteImageCmdData.getImageId())
                            .fluentPut("idc", idcInfo.getIdc())
                            .fluentPut("bucket", idcInfo.getBucket())
                    ))
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }
        return persistence(safety, jobId, jobCmd, jobDetailList, (JSONObject) JSONObject.toJSON(deleteImageCmdData),
                successJobId -> kvLogger.i(), error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
    }

    private R<SendJobResult> sendCreate(JobInnerCmd<?> jobCmd) {
        String jobId = jobCmd.getJobId();
        CreateImageCmdData createImageCmdData;
        if (jobCmd.getJobData() instanceof CreateImageCmdData) {
            createImageCmdData = (CreateImageCmdData) jobCmd.getJobData();
        } else {
            createImageCmdData = ((JSONObject) jobCmd.getJobData()).toJavaObject(CreateImageCmdData.class);
        }

        // build default result
        SendJobResult sendJobResult = buildDefaultSendResult(jobId, createImageCmdData.getIdcInfos().size());
        // send job to edge by service
        for (CreateImageCmdData.IdcInfo idcInfo : createImageCmdData.getIdcInfos()) {
            String jobDetailId = jobId + StringPool.DASH + createImageCmdData.getImageId();

            JobParams.ImageCreate jobParams = JobParams.ImageCreate.newBuilder()
                    .setImageId(createImageCmdData.getImageId())
                    .setImageName(createImageCmdData.getImageName())
                    .setImageVer(createImageCmdData.getImageVer())
                    .setDownloadUrl(createImageCmdData.getDownloadUrl())
                    .setMd5(createImageCmdData.getMd5())
                    .setBucket(idcInfo.getBucket())
                    .setJobDetailId(jobDetailId)
                    .build();
            R<?> sendR = sendJobToEdge(idcInfo.getIdc(), jobCmd, jobParams.toByteString());
            if (sendR.getCode() == R.ok().getCode()) {
                sendJobResult.getSuccess().add(jobDetailId);
            } else {
                sendJobResult.getFail().add(jobDetailId);
            }
        }
        boolean isAllSuccess = sendJobResult.getSuccess().size() == sendJobResult.getTotalJobCount();
        return R.<SendJobResult>builder()
                .code(isAllSuccess ? 0 : SysCode.x00000701.getValue())
                .msg(isAllSuccess ? null : SysCode.x00000701.getMsg())
                .data(sendJobResult)
                .build();
    }

    private R<SendJobResult> sendDelete(JobInnerCmd<?> jobCmd) {
        String jobId = jobCmd.getJobId();
        List<JobParams.ImageDeleteTarget> targetList = new ArrayList<>();
        DeleteImageCmdData deleteImageCmdData;
        if (jobCmd.getJobData() instanceof DeleteImageCmdData) {
            deleteImageCmdData = (DeleteImageCmdData) jobCmd.getJobData();
        } else {
            deleteImageCmdData = ((JSONObject) jobCmd.getJobData()).toJavaObject(DeleteImageCmdData.class);
        }

        // build default result
        SendJobResult sendJobResult = buildDefaultSendResult(jobId, deleteImageCmdData.getIdcInfos().size());
        // send job to edge by service
        for (DeleteImageCmdData.IdcInfo idcInfo : deleteImageCmdData.getIdcInfos()) {
            String jobDetailId = jobId + StringPool.DASH + deleteImageCmdData.getImageId();

            targetList.add(JobParams.ImageDeleteTarget.newBuilder()
                    .setImageId(deleteImageCmdData.getImageId())
                    .setBucket(idcInfo.getBucket())
                    .setJobDetailId(jobDetailId)
                    .build());

            JobParams.ImageDelete jobParams = JobParams.ImageDelete.newBuilder()
                    .addAllTarget(targetList)
                    .build();
            R<?> sendR = sendJobToEdge(idcInfo.getIdc(), jobCmd, jobParams.toByteString());
            if (sendR.getCode() == R.ok().getCode()) {
                sendJobResult.getSuccess().add(jobDetailId);
            } else {
                sendJobResult.getFail().add(jobDetailId);
            }
        }
        boolean isAllSuccess = sendJobResult.getSuccess().size() == sendJobResult.getTotalJobCount();
        return R.<SendJobResult>builder()
                .code(isAllSuccess ? 0 : SysCode.x00000701.getValue())
                .msg(isAllSuccess ? null : SysCode.x00000701.getMsg())
                .data(sendJobResult)
                .build();
    }

    private String persistence(boolean safety, String jobId, JobInnerCmd<?> jobCmd, List<JobDetail> jobDetailList, JSONObject jobParams,
                               Consumer<String> consumer, Consumer<Exception> exceptionConsumer) {
        if (safety) {
            try {
                safetyPersistenceJob(jobId, jobCmd, jobParams, "", jobDetailList);
                consumer.accept(jobId);
                return jobId;
            } catch (Exception ex) {
                exceptionConsumer.accept(ex);
                return "";
            }
        } else {
            persistenceJob(jobId, jobCmd, jobParams, "", jobDetailList);
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
                // detail job success, to create image download inf
                if (jobStatus == JobStatusEnum.SUCCESS) {
                    JSONObject outputJson = JSONObject.parseObject(reportMessage.getOutput());
                    String imageId = outputJson.getString("imageId");
                    String imageVer = outputJson.getString("imageVer");
                    String idcStoragePath = outputJson.getString("idcStoragePath");
                    String netStoragePath = outputJson.getString("netStoragePath");
                    String md5 = outputJson.getString("md5");
                    ImageDownloadInfService imageDownloadInfService = SpringContextHolder.getBean(ImageDownloadInfService.class);
                    // add image download info
                    transactionTemplate.executeWithoutResult(status -> {
                        KvLogger kvLogger = KvLogger.instance(this)
                                .p(LogFieldConstants.EVENT, CenterEvent.PROCESS_JOB_NOTIFY)
                                .p(LogFieldConstants.ACTION, CenterEvent.Action.CREATE_IMAGE)
                                .p(LogFieldConstants.TID, reportMessage.getTid())
                                .p(LogFieldConstants.TRACE_ID, reportMessage.getTraceId())
                                .p("CreateImageInfo", reportMessage.getOutput())
                                .p(HostStackConstants.JOB_ID, reportMessage.getJobId())
                                .p(HostStackConstants.JOB_DETAIL_ID, reportMessage.getJobDetailId());
                        try {
                            imageDownloadInfService.save(ImageDownloadInf.builder()
                                    .imageId(imageId)
                                    .imageVer(imageVer)
                                    .netDownloadUrl(netStoragePath)
                                    .localDownloadUrl(idcStoragePath)
                                    .md5(md5)
                                    .region(reportMessage.getRegion())
                                    .idc(reportMessage.getIdc())
                                    .tenantId(reportMessage.getTid())
                                    .build());
                            kvLogger.i();
                        } catch (Exception ex) {
                            status.setRollbackOnly();
                            kvLogger.p(LogFieldConstants.ERR_MSG, ex.getMessage())
                                    .e(ex);
                        }
                    });
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
                // detail job success to delete image
                if (jobStatus == JobStatusEnum.SUCCESS) {
                    JSONObject outputJson = JSONObject.parseObject(reportMessage.getOutput());
                    String imageId = outputJson.getString("imageId");
                    String imageVer = outputJson.getString("imageVer");
                    ImageDownloadInfService imageDownloadInfService = SpringContextHolder.getBean(ImageDownloadInfService.class);
                    // delete image download info
                    imageDownloadInfService.remove(Wrappers.lambdaQuery(ImageDownloadInf.class)
                            .eq(ImageDownloadInf::getImageId, imageId)
                            .eq(ImageDownloadInf::getImageVer, imageVer)
                            .eq(ImageDownloadInf::getTenantId, reportMessage.getTid()));
                    KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, CenterEvent.PROCESS_JOB_NOTIFY)
                            .p(LogFieldConstants.ACTION, CenterEvent.Action.DELETE_IMAGE_DOWNLOAD_INF)
                            .p(LogFieldConstants.TID, reportMessage.getTid())
                            .p(LogFieldConstants.TRACE_ID, reportMessage.getTraceId())
                            .p(HostStackConstants.JOB_ID, reportMessage.getJobId())
                            .p(HostStackConstants.JOB_DETAIL_ID, reportMessage.getJobDetailId())
                            .p("ImageId", imageId)
                            .p("ImageVer", imageVer)
                            .i();
                }
                // complete job
                jobProcessService.completeJob(jobStatus, reportMessage.getJobId(), reportMessage.getJobDetailId(),
                        buildJobResult(reportMessage), (jobId, finalJobStatus, output) -> {
                            // if all image download info is delete, to delete imageInfo
                            if (finalJobStatus == JobStatusEnum.SUCCESS) {
                                JSONObject outputJson = JSONObject.parseObject(reportMessage.getOutput());
                                String imageId = outputJson.getString("imageId");
                                String imageVer = outputJson.getString("imageVer");
                                ImageInfoService imageInfoService = SpringContextHolder.getBean(ImageInfoService.class);
                                imageInfoService.remove(Wrappers.lambdaQuery(ImageInfo.class)
                                        .eq(ImageInfo::getImageId, imageId)
                                        .eq(ImageInfo::getImageVer, imageVer)
                                        .eq(ImageInfo::getTenantId, reportMessage.getTid()));
                                KvLogger.instance(this)
                                        .p(LogFieldConstants.EVENT, CenterEvent.PROCESS_JOB_NOTIFY)
                                        .p(LogFieldConstants.ACTION, CenterEvent.Action.DELETE_IMAGE)
                                        .p(LogFieldConstants.TID, reportMessage.getTid())
                                        .p(LogFieldConstants.TRACE_ID, reportMessage.getTraceId())
                                        .p(HostStackConstants.JOB_ID, reportMessage.getJobId())
                                        .p(HostStackConstants.JOB_DETAIL_ID, reportMessage.getJobDetailId())
                                        .p("ImageId", imageId)
                                        .p("ImageVer", imageVer)
                                        .i();
                            }
                        });
            }
        }
    }
}
