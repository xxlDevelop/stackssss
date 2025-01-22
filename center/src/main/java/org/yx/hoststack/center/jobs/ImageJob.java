package org.yx.hoststack.center.jobs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.enums.JobStatusEnum;
import org.yx.hoststack.center.entity.JobDetail;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.jobs.cmd.image.CreateImageCmdData;
import org.yx.hoststack.center.jobs.cmd.image.DeleteImageCmdData;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Service("image")
public class ImageJob extends BaseJob implements IJob {

    public ImageJob(JobInfoService jobInfoService, JobDetailService jobDetailService,
                    CenterService centerService, ServerCacheInfoServiceBiz serverCacheInfoServiceBiz,
                    TransactionTemplate transactionTemplate) {
        super(jobInfoService, jobDetailService, centerService, serverCacheInfoServiceBiz, transactionTemplate);
    }

    @Override
    public String doJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case CREATE -> create(jobCmd, false);
            case DELETE -> delete(jobCmd, false);
            default -> "";
        };
    }

    @Override
    public String safetyDoJob(JobInnerCmd<?> jobCmd) {
        return switch (jobCmd.getJobSubType()) {
            case CREATE -> create(jobCmd, true);
            case DELETE -> delete(jobCmd, true);
            default -> "";
        };
    }

    private String create(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        CreateImageCmdData createImageCmdData = (CreateImageCmdData) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .flat("CreateImageInfo", createImageCmdData)
                .p(HostStackConstants.JOB_ID, jobId);

        JobParams.ImageCreate jobParams = JobParams.ImageCreate.newBuilder()
                .setImageId(createImageCmdData.getImageId())
                .setImageName(createImageCmdData.getImageName())
                .setImageVer(createImageCmdData.getImageVer())
                .setDownloadUrl(createImageCmdData.getDownloadUrl())
                .setMd5(createImageCmdData.getMd5())
                .setBucket(createImageCmdData.getBucket())
                .build();
        return persistence(safety, jobId, jobCmd, null, (JSONObject) JSONObject.toJSON(createImageCmdData),
                successJobId -> {
                    kvLogger.i();
                    sendJobToEdge(jobCmd, jobParams.toByteString());
                }, error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
    }

    private String delete(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        List<DeleteImageCmdData> deleteImageCmdData = (List<DeleteImageCmdData>) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("DeleteImageInfo", JSON.toJSONString(deleteImageCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JobParams.ImageDeleteTarget> targetList = new ArrayList<>();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (DeleteImageCmdData cmdData : deleteImageCmdData) {
            targetList.add(JobParams.ImageDeleteTarget.newBuilder()
                    .setImageId(cmdData.getImageId())
                    .setBucket(cmdData.getBucket())
                    .setJobDetailId(jobId + StringPool.DASH + cmdData.getImageId())
                    .build());

            jobDetailList.add(JobDetail.builder()
                    .jobId(jobId)
                    .jobDetailId(jobId + StringPool.DASH + cmdData.getImageId())
                    .jobHost(cmdData.getImageId())
                    .jobStatus(JobStatusEnum.PROCESSING.getName())
                    .jobProgress(RandomUtils.insecure().randomInt(1, 50))
                    .jobParams("{\"imageId\":\"" + cmdData.getImageId() + "\",\"bucket\":\"" + cmdData.getBucket() + "\",\"idc\":\"" + cmdData.getIdc() + "\"}")
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .build());
        }


        JobParams.ImageDelete jobParams = JobParams.ImageDelete.newBuilder()
                .addAllTarget(targetList)
                .build();
        return persistence(safety, jobId, jobCmd, jobDetailList, null,
                successJobId -> {
                    kvLogger.i();
                    sendJobToEdge(jobCmd, jobParams.toByteString());
                }, error -> kvLogger.p(LogFieldConstants.ERR_MSG, error.getMessage()).e(error));
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
}
