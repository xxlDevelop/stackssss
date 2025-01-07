package org.yx.hoststack.center.common.req.job;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description : JonDetail - mount volume DTO
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
public class JobDetailMountVolumeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -3969129822978618651L;
    /**
     * Host resource ID to which the container belongs, created through the hoststack platform, this field is not null
     */
    private String hostId;
    /**
     *  volume ID
     */
    private String volumeId;
    /**
     * The container ID currently bound to the data volume
     */
    private String mountContainerId;
    /**
     * Unique identifier of the basic disk image of the data volume
     */
    private String baseVolumeId;
}
