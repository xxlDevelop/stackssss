package org.yx.hoststack.center.common.req.container;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Description : Container create request DTO
 * @Author : Lee666
 * @Date : 2024/12/25
 * @Version : 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class ContainerCreateReqDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 880943356020609871L;
    /**
     * Partition identifier, if not passed in, locate the region to which the hostId belongs based on hostId addressing
     */
    private String region;
    /**
     * Computer room identification, if not passed in, locate the idc to which the hostId belongs based on hostId addressing
     */
    private String idc;
    /**
     * Unique ID of the host computer
     */
    @NotBlank(message = "host id cannot be empty")
    @Length(message = "The host id contains a maximum of 64 characters", max = 64)
    private String hostId;
    /**
     * Container virtualization orchestration configuration
     */
    @Valid
    private ContainerCreateProfileReqDTO profile;
    /**
     * Image ID, the interface uses this ID to query the download address and other information of the image, and then sends it to the host machine to perform the container creation operation
     */
    @NotBlank(message = "The image id cannot be empty")
    @Length(message = "The name contains a maximum of 128 characters", max = 128)
    private String imageId;
    /**
     * Number of containers to be created
     */
    @NotNull(message = "count cannot be empty")
    @Min(value = 1, message = "The minimum value of count is 1")
    private Integer count;
    /**
     * base volume id
     */
    private String baseVolumeId;
    /**
     * user volume id
     */
    private String userVolumeId;
}
