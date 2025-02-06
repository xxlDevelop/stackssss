package org.yx.hoststack.center.common.req.volume;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static org.yx.hoststack.center.common.constant.VolumeConstants.DISK_TYPE_LOCAL;
import static org.yx.hoststack.center.common.constant.VolumeConstants.VOLUME_TYPE_USER;

@Data
public class CreateVolumeReq {
    private String zone;
    private String region;
    private String relay;
    private String idc;

    @NotBlank(message = "hostId cannot be empty")
    private String hostId;

    @Min(value = 1, message = "The minimum value of volumeCount is 1")
    private Integer volumeCount = 1;

    @Min(value = 1, message = "The minimum volumeSize value is 1")
    private Integer volumeSize = 128;

    /**
     * Tenant ID of the image owner
     * If uploaded by administrator, tenantId is 10000
     */
    @NotNull(message = "Tenant ID cannot be empty")
    private Long tenantId;
    /**
     * md5
     */
    @NotBlank(message = "md5 cannot be empty")
    private String md5;

    private String volumeType = VOLUME_TYPE_USER;
    private String downloadUrl;
    private String diskType = DISK_TYPE_LOCAL;
}