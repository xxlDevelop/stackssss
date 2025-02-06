package org.yx.hoststack.center.common.req.volume;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.yx.hoststack.center.common.req.PageReq;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeMountRelReq extends PageReq {
    /**
     * Region identifier
     */
    private String region;

    /**
     * IDC identifier
     */
    private String idc;

    /**
     * Host ID / Storage System ID
     */
    private String vmHost;

    /**
     * Volume ID
     */
    private String volumeId;

    /**
     * Container ID
     */
    private String cid;
}