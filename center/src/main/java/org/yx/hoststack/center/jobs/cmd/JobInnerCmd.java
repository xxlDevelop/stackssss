package org.yx.hoststack.center.jobs.cmd;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobInnerCmd<T> extends JobCmd<T> {

    private String jobId;
    private String nextJobId;
    private String rootJobId;
    private int runOrder;
}
