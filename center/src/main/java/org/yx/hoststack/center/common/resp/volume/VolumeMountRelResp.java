package org.yx.hoststack.center.common.resp.volume;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeMountRelResp {
    /**
     * Region identifier
     */
    private String region;

    /**
     * IDC identifier
     */
    private String idc;

    /**
     * User volume ID
     */
    private String volumeId;

    /**
     * Base volume ID
     */
    private String baseVolumeId;

    /**
     * Volume size (sparse type) in GB
     */
    private Long volumeSize;

    /**
     * Volume disk type
     * local: Local disk
     * remote: Remote disk
     */
    private String diskType;

    /**
     * Host ID / Storage System ID
     */
    private String vmHost;

    /**
     * Container ID
     */
    private String cid;

    /**
     * Creation timestamp
     */
    private Long createAt;

    /**
     * Mount timestamp
     */
    private Long mountAt;
}