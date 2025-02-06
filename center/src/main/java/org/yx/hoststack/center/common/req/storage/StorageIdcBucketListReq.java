package org.yx.hoststack.center.common.req.storage;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.yx.hoststack.center.common.req.PageReq;

@EqualsAndHashCode(callSuper = true)
@Data
public class StorageIdcBucketListReq extends PageReq {
    private String region;
    private String idc;

}