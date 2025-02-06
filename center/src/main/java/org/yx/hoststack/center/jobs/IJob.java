package org.yx.hoststack.center.jobs;

import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;
import org.yx.hoststack.center.queue.message.JobReportMessage;
import org.yx.lib.utils.util.R;

public interface IJob {
    String createJob(JobInnerCmd<?> jobCmd);

    String safetyCreateJob(JobInnerCmd<?> jobCmd);

    R<SendJobResult> sendJob(JobInnerCmd<?> jobCmd);

    void processJobReportResult(JobReportMessage reportMessage);
}
