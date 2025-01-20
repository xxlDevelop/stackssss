package org.yx.hoststack.center.jobs;

import org.yx.hoststack.center.jobs.cmd.JobInnerCmd;

public interface IJob {
    String doJob(JobInnerCmd<?> jobCmd);

    String safetyDoJob(JobInnerCmd<?> jobCmd);
}
