package org.yx.hoststack.center.jobs;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.queue.message.JobReportMessage;
import org.yx.hoststack.center.service.biz.ServerCacheInfoServiceBiz;
import org.yx.hoststack.center.service.center.CenterService;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;

@Service("parent")
public class ParentJob extends BaseJob implements IJob {

    @Override
    public String createJob(JobInnerCmd<?> jobCmd) {
        return doParentJob(jobCmd, false);
    }

    @Override
    public String safetyCreateJob(JobInnerCmd<?> jobCmd) {
        return doParentJob(jobCmd, true);
    }

    @Override
    public R<SendJobResult> sendJob(JobInnerCmd<?> jobCmd) {
        return R.ok();
    }

    @Override
    public void processJobReportResult(JobReportMessage reportMessage) {

    }

    private String doParentJob(JobInnerCmd<?> jobCmd, boolean safety) {
        String jobId = jobCmd.getJobId();
        KvLogger kvLogger = KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CREATE_JOB)
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
