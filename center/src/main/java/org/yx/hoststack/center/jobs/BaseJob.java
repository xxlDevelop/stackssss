package org.yx.hoststack.center.jobs;


import com.alibaba.fastjson.JSONObject;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.yx.hoststack.center.common.enums.JobStatusEnum;
import org.yx.hoststack.center.entity.JobDetail;
import org.yx.hoststack.center.entity.JobInfo;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.service.JobDetailService;
import org.yx.hoststack.center.service.JobInfoService;

import java.sql.Timestamp;
import java.util.List;

public class BaseJob {
    protected final JobInfoService jobInfoService;
    protected final JobDetailService jobDetailService;
    protected final TransactionTemplate transactionTemplate;

    public BaseJob(JobInfoService jobInfoService,
                   JobDetailService jobDetailService,
                   TransactionTemplate transactionTemplate) {
        this.jobInfoService = jobInfoService;
        this.jobDetailService = jobDetailService;
        this.transactionTemplate = transactionTemplate;
    }

    protected void safetyPersistenceJob(String jobId, JobInnerCmd<?> jobCmd, JSONObject jobParams,
                                        String externalParams, List<JobDetail> jobDetailList) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    persistenceJob(jobId, jobCmd, jobParams, externalParams, jobDetailList);
                } catch (Exception ex) {
                    status.setRollbackOnly();
                    throw ex;
                }
            }
        });
    }

    protected void persistenceJob(String jobId, JobInnerCmd<?> jobCmd, JSONObject jobParams,
                                 String externalParams, List<JobDetail> jobDetailList) {
        int process = 0;
        if (jobDetailList != null) {
            process = (int) jobDetailList.stream().mapToInt(JobDetail::getJobProgress).average().orElse(0);
        }
        JobInfo jobInfo = JobInfo.builder()
                .jobId(jobId)
                .jobType(jobCmd.getJobType().getName())
                .jobSubType(jobCmd.getJobSubType().getName())
                .runOrder(jobCmd.getRunOrder())
                .jobStatus(JobStatusEnum.PROCESSING.getName())
                .jobProgress(process)
                .zone(jobCmd.getZone())
                .region(jobCmd.getRegion())
                .idc(jobCmd.getIdc())
                .relay(jobCmd.getRelay())
                .jobDetailNum(jobDetailList == null ? 0 : jobDetailList.size())
                .tenantId(jobCmd.getTenantId())
                .jobParams(jobParams != null ? jobParams.toJSONString() : null)
                .externalParams(externalParams)
                .createAt(new Timestamp(System.currentTimeMillis()))
                .nextJobId(jobCmd.getNextJobId())
                .rootJobId(jobCmd.getRootJobId())
                .build();
        jobInfoService.insert(jobInfo);
        if (jobDetailList != null && !jobDetailList.isEmpty()) {
            jobDetailService.saveBatch(jobDetailList);
        }
    }
}
