package org.yx.hoststack.center.common.resp.volume;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VolumeListResp {
    /**
     * Volume ID
     */
    private String volumeId;

    /**
     * Volume size (sparse type)
     * Unit: GB, Default: 128GB
     */
    private Integer volumeSize;

    /**
     * Volume disk type
     * Local: Local disk type
     * Remote: Remote disk type
     * Default: Local
     */
    private String diskType;

    /**
     * Volume type
     * base: Base volume
     * user: User volume
     */
    private String volumeType;

    /**
     * Mount status
     * mount: Mounted
     * unmount: Unmounted
     */
    private String status;

    /**
     * Volume creation timestamp
     */
    private Long createAt;

    /**
     * Volume mount timestamp
     */
    private Long mountAt;
}