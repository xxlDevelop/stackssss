package org.yx.hoststack.center.jobs.cmd;

import lombok.*;
import org.yx.hoststack.center.common.enums.JobSubTypeEnum;
import org.yx.hoststack.center.common.enums.JobTypeEnum;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobCmd<T> {
    private String zone;
    private String region;
    private String idc;
    private String relay;
    private long tenantId;
    private JobTypeEnum jobType;
    private JobSubTypeEnum jobSubType;
    private T jobData;
}
