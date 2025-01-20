package org.yx.hoststack.edge.apiservice.storagesvc.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DistributeFileReq {

    /**
     * objectKey : string
     * downloadUrl : string
     * md5: md5
     * tid : 0
     * bucket : string
     * jobID : string
     * callbackUrl : string
     */

    private String objectKey;
    private String downloadUrl;
    private String md5;
    private long tid;
    private String bucket;
    private String jobID;
    private String callbackUrl;
}
