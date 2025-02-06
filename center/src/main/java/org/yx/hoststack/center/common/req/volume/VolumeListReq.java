package org.yx.hoststack.center.common.req.volume;

import lombok.*;
import org.yx.hoststack.center.common.req.PageReq;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeListReq extends PageReq {

    /**
     * Host ID / Storage System ID
     */
    private String vmHost;

    /**
     * Volume ID
     */
    private String volumeId;

    /**
     * Volume disk type
     * local: Local disk
     * net: Network disk
     */
    private String diskType;

    /**
     * Volume type
     * user: User volume
     * base: Base volume
     */
    private String volumeType;
}