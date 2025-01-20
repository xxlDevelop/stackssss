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
import org.yx.hoststack.common.HostStackConstants;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.StringPool;

import java.sql.Timestamp;
import java.util.List;

@Service("image")
public class ImageJob extends BaseJob implements IJob {
    public ImageJob(JobInfoService jobInfoService, JobDetailService jobDetailService,
                    TransactionTemplate transactionTemplate) {
        super(jobInfoService, jobDetailService, transactionTemplate);
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
        JSONObject jobParams = new JSONObject()
                .fluentPut("imageId", createImageCmdData.getImageId())
                .fluentPut("imageName", createImageCmdData.getImageName())
                .fluentPut("imageVer", createImageCmdData.getImageVer())
                .fluentPut("downloadUrl", createImageCmdData.getDownloadUrl())
                .fluentPut("md5", createImageCmdData.getMd5())
                .fluentPut("bucket", createImageCmdData.getBucket())
                .fluentPut("idc", createImageCmdData.getIdc());
        return persistence(safety, jobId, jobCmd, null, kvLogger);
    }

    private String delete(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        List<DeleteImageCmdData> deleteImageCmdData = (List<DeleteImageCmdData>) jobCmd.getJobData();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p("DeleteImageInfo", JSON.toJSONString(deleteImageCmdData))
                .p(HostStackConstants.JOB_ID, jobId);

        List<JSONObject> targetList = Lists.newArrayList();
        List<JobDetail> jobDetailList = Lists.newArrayList();
        for (DeleteImageCmdData cmdData : deleteImageCmdData) {
            JSONObject target = new JSONObject()
                    .fluentPut("imageId", cmdData.getImageId())
                    .fluentPut("bucket", cmdData.getBucket())
                    .fluentPut("jobDetailId", jobId + StringPool.DASH + cmdData.getImageId());
            targetList.add(target);

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
        JSONObject jobParams = new JSONObject()
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
