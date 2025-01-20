package org.yx.hoststack.center.jobs;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.service.JobDetailService;
import org.yx.hoststack.center.service.JobInfoService;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

@Service("parent")
public class ParentJob extends BaseJob implements IJob {
    public ParentJob(JobInfoService jobInfoService, JobDetailService jobDetailService,
                     TransactionTemplate transactionTemplate) {
        super(jobInfoService, jobDetailService, transactionTemplate);
    }

    @Override
    public String doJob(JobInnerCmd<?> jobCmd) {
        return doParentJob(jobCmd, false);
    }

    @Override
    public String safetyDoJob(JobInnerCmd<?> jobCmd) {
        return doParentJob(jobCmd, true);
    }

    private String doParentJob(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.DoJob)
                .p(LogFieldConstants.ACTION, String.format("%s-%s", jobCmd.getJobType().getName(), jobCmd.getJobSubType().getName()))
                .p(HostStackConstants.JOB_ID, jobId);
        if (safety) {
            try {
                safetyPersistenceJob(jobId, jobCmd, null, "", null);
                kvLogger.i();
                return jobId;
            } catch (Exception ex) {
                kvLogger.p(LogFieldConstants.ERR_MSG, ex.getMessage())
                        .e(ex);
                return "";
            }
        } else {
            persistenceJob(jobId, jobCmd, null, "", null);
            kvLogger.i();
            return jobId;
        }
    }
}
