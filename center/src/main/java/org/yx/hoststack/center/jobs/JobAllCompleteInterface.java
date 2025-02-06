package org.yx.hoststack.center.jobs;

import org.yx.hoststack.center.common.enums.JobStatusEnum;

public interface JobAllCompleteInterface {
    void allComplete(String jobId, JobStatusEnum finalJobStatus, String output);
}
