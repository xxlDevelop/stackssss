package org.yx.hoststack.center.queue.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobReportMessage {
    private String jobId;
    private String traceId;
    private String jobDetailId;
    private String status;
    private int code;
    private String msg;
    private int progress;
    private String output;
    private String jobType;
    private String jobSubType;
    private String region;
    private String idc;
    private long tid;
}
