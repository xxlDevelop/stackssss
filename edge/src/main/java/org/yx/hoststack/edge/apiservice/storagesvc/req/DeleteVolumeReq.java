package org.yx.hoststack.edge.apiservice.storagesvc.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteVolumeReq {
    private String volumeName;
    private long tid;
}
