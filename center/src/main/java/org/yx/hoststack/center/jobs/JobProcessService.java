package org.yx.hoststack.center.jobs;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.yx.hoststack.center.common.enums.JobStatusEnum;
import org.yx.hoststack.center.entity.JobDetail;
import org.yx.hoststack.center.entity.JobInfo;
import org.yx.hoststack.center.service.JobDetailService;
import org.yx.hoststack.center.service.JobInfoService;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobProcessService {
    private final JobInfoService jobInfoService;
    private final JobDetailService jobDetailService;
    private final TransactionTemplate transactionTemplate;

    public JobInfo getJob(String jobId) {
        return jobInfoService.getOne(Wrappers.lambdaQuery(JobInfo.class).eq(JobInfo::getJobId, jobId));
    }

    public JobInfo getJobWithoutJobCmd(String jobId) {
        return jobInfoService.getOne(Wrappers.lambdaQuery(JobInfo.class).eq(JobInfo::getJobId, jobId)
                .select(JobInfo::getJobId, JobInfo::getJobType, JobInfo::getJobSubType, JobInfo::getNextJobId,
                        JobInfo::getRootJobId, JobInfo::getRunOrder, JobInfo::getJobStatus, JobInfo::getJobProgress,
                        JobInfo::getJobDetailNum, JobInfo::getTenantId, JobInfo::getJobParams, JobInfo::getExternalParams,
                        JobInfo::getRunTime, JobInfo::getCreateAt, JobInfo::getLastUpdateAt));
    }

    public JobDetail getJobDetail(String jobId, String jobDetailId) {
        return jobDetailService.getOne(Wrappers.lambdaQuery(JobDetail.class)
                .eq(JobDetail::getJobId, jobId).eq(JobDetail::getJobDetailId, jobDetailId));
    }

    public long getJobDetailStartTime(String jobId, String jobDetailId) {
        JobDetail jobDetail = jobDetailService.getOne(Wrappers.lambdaQuery(JobDetail.class)
                .eq(JobDetail::getJobId, jobId).eq(JobDetail::getJobDetailId, jobDetailId)
                .select(JobDetail::getCreateAt));
        return jobDetail.getCreateAt().getTime();
    }

    public void updateDetailProgress(String jobId, String jobDetailId, int newProgress) {
        jobDetailService.update(Wrappers.lambdaUpdate(JobDetail.class)
                .set(JobDetail::getJobProgress, newProgress)
                .eq(JobDetail::getJobId, jobId)
                .eq(JobDetail::getJobDetailId, jobDetailId)
        );
    }

    public void insertJob(JobInfo jobInfo) {
        jobInfoService.insert(jobInfo);
    }

    public void saveBatchDetailJobs(List<JobDetail> jobDetailList) {
        jobDetailService.saveBatch(jobDetailList);
    }

    public void updateJobInfo(LambdaUpdateWrapper<JobInfo> updateWrapper) {
        jobInfoService.update(updateWrapper);
    }

    public void updateJobDetail(LambdaUpdateWrapper<JobDetail> updateWrapper) {
        jobDetailService.update(updateWrapper);
    }

    public long jobDetailCountByStatus(String jobId, List<String> jobStatus) {
        return jobDetailService.countByStatus(jobId, jobStatus);
    }

    public boolean jobIsAllSuccess(String jobId) {
        long detailCompleteCount = jobDetailCountByStatus(jobId,
                Lists.newArrayList(JobStatusEnum.PROCESSING.name(), JobStatusEnum.FAIL.name(), JobStatusEnum.WAIT.name()));
        return detailCompleteCount == 0;
    }

    public boolean completeJob(JobStatusEnum jobStatus, String jobId, String jobDetailId, JSONObject jobResult, JobAllCompleteInterface allComplete) {
        // TODO need try-catch, transaction
        long jobDetailStartTime = getJobDetailStartTime(jobId, jobDetailId);
        LambdaUpdateWrapper<JobDetail> updateJobDetailWrapper = Wrappers.lambdaUpdate(JobDetail.class)
                .set(JobDetail::getJobStatus, jobStatus.name())
                .set(JobDetail::getLastUpdateAt, new Timestamp(System.currentTimeMillis()))
                .set(JobDetail::getJobResult, jobResult.toJSONString())
                .set(JobDetail::getJobProgress, 100)
                .set(JobDetail::getRunTime, (System.currentTimeMillis() - jobDetailStartTime) / 1000)
                .eq(JobDetail::getJobId, jobId)
                .eq(JobDetail::getJobDetailId, jobDetailId);
        jobDetailService.update(updateJobDetailWrapper);

        JobInfo jobInfo = getJobWithoutJobCmd(jobId);
        long detailCompleteCount = jobDetailCountByStatus(jobId, Lists.newArrayList(JobStatusEnum.SUCCESS.name(), JobStatusEnum.FAIL.name()));
        int jobProgress = ((int) detailCompleteCount / jobInfo.getJobDetailNum()) * 100;
        if (jobProgress > 100) {
            jobProgress = 100;
        }
        LambdaUpdateWrapper<JobInfo> updateWrapper = Wrappers.lambdaUpdate(JobInfo.class)
                .set(JobInfo::getJobProgress, jobProgress)
                .set(JobInfo::getLastUpdateAt, new Timestamp(System.currentTimeMillis()))
                .eq(JobInfo::getJobId, jobId);
        // all complete
        JobStatusEnum jobFinalStatus = jobStatus;
        if (detailCompleteCount == jobInfo.getJobDetailNum()) {
            if (jobFinalStatus == JobStatusEnum.SUCCESS && !jobIsAllSuccess(jobId)) {
                jobFinalStatus = JobStatusEnum.FAIL;
            }
            updateWrapper = updateWrapper
                    .set(JobInfo::getRunTime, (System.currentTimeMillis() - jobInfo.getCreateAt().getTime()) / 1000)
                    .set(JobInfo::getJobStatus, jobFinalStatus.name());
        }
        updateJobInfo(updateWrapper);
        if (detailCompleteCount == jobInfo.getJobDetailNum() && allComplete != null) {
            allComplete.allComplete(jobId, jobFinalStatus, jobResult == null ? null : jobResult.getString("output"));
        }
        return true;
    }

    public void updateJobChainFail(String rootJobId, String failMsg) {
        transactionTemplate.executeWithoutResult(status -> {
            // update eq rootJobId jobs to fail
            jobInfoService.update(Wrappers.lambdaUpdate(JobInfo.class)
                    .set(JobInfo::getJobStatus, JobStatusEnum.FAIL.name())
                    .set(JobInfo::getJobProgress, 100)
                    .eq(JobInfo::getRootJobId, rootJobId)
                    .eq(JobInfo::getLastUpdateAt, new Timestamp(System.currentTimeMillis()))
                    .eq(JobInfo::getJobStatus, Lists.newArrayList(JobStatusEnum.PROCESSING.name(), JobStatusEnum.WAIT.name())));
            // find rootJob to fail
            jobInfoService.update(Wrappers.lambdaUpdate(JobInfo.class)
                    .set(JobInfo::getJobStatus, JobStatusEnum.FAIL.name())
                    .set(JobInfo::getJobProgress, 100)
                    .eq(JobInfo::getJobId, rootJobId)
                    .eq(JobInfo::getLastUpdateAt, new Timestamp(System.currentTimeMillis())));
            // select eq rootJobId jobs
            List<String> jobIds = jobInfoService.list(Wrappers.lambdaQuery(JobInfo.class)
                    .eq(JobInfo::getRootJobId, rootJobId)
                    .eq(JobInfo::getJobStatus, Lists.newArrayList(JobStatusEnum.PROCESSING.name(), JobStatusEnum.WAIT.name()))
                    .select(JobInfo::getJobId)).stream().map(JobInfo::getJobId).toList();
            // update job detail to fail
            jobDetailService.update(Wrappers.lambdaUpdate(JobDetail.class)
                    .set(JobDetail::getJobStatus, JobStatusEnum.FAIL.name())
                    .set(JobDetail::getLastUpdateAt, new Timestamp(System.currentTimeMillis()))
                    .set(JobDetail::getJobProgress, 100)
                    .set(JobDetail::getJobResult, failMsg)
                    .in(JobDetail::getJobId, jobIds));
        });
    }
}
