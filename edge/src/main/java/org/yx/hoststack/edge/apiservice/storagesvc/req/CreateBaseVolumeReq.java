package org.yx.hoststack.edge.apiservice.storagesvc.req;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Data
@Getter
@Setter
public class CreateBaseVolumeReq {

    /**
     * volumeName : string          volumeName
     * size : 0                     volumeSize unit of GB
     * initMode : string            default or remote
     * initDownloadUrl : string     initMode eq remote, url of download source data
     * poolName : string            fix poolName
     * tid : int                    tid
     */

    private String volumeName;
    private int size;
    private String initMode;
    private String initDownloadUrl;
    private String poolName;
    private long tid;
}
