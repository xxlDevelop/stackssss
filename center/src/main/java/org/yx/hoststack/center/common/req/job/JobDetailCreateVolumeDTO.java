package org.yx.hoststack.center.common.req.job;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description : JonDetail - create base/user volume DTO
 * @Author : Lee666
 * @Date : 2025/1/6
 * @Version : 1.0
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class JobDetailCreateVolumeDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 4954472443361135669L;

    /**
     * Host resource ID to which the container belongs, created through the hoststack platform, this field is not null
     */
    private String hostId;
    /**
     * ID
     */
    private String volumeId;
    /**
     * Sparse size of data volume, unit: KB
     */
    private Long volumeSize;
    /**
     * Storage volume type: base or user
     */
    private String volumeType;
    /**
     * Data volume disk type, LOCAL: Local disk, REMOTE:  Network disk
     */
    private String diskType;
    /**
     * The metadata address to be downloaded when creating a non empty storage volume
     */
    private String downloadUrl;
}
