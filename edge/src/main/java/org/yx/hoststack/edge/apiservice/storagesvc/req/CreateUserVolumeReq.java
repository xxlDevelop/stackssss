package org.yx.hoststack.edge.apiservice.storagesvc.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserVolumeReq {


    /**
     * volumeName : string          volumeName
     * snapshotName : string        snapshotName
     * tid : long                   tid
     */

    private String volumeName;
    private String snapshotName;
    private long tid;
}
