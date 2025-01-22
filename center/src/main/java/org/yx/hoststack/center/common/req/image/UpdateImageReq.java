package org.yx.hoststack.center.common.req.image;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for creating image information
 *
 * @author lyc
 * @since 2025-01-20
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateImageReq {

    /**
     * Id
     */
    private Long id;

    /**
     * zone identifier for specific deployment
     * Optional: If provided, image creation instruction will be sent only to matching regions
     */
    private String zone;

    /**
     * Region identifier for specific deployment
     * Optional: If provided, image creation instruction will be sent only to matching regions
     */
    private String region;

    /**
     * Relay node identifier
     * Optional: If provided, image creation instruction will be sent only to matching relay nodes
     */
    private String relay;

    /**
     * IDC identifier
     * Optional: If provided, image creation instruction will be sent only to matching IDC nodes
     */
    private String idc;

    /**
     * Image name
     */
    @NotBlank(message = "Image name cannot be empty")
    private String imageName;

    /**
     * Image version
     */
    @NotBlank(message = "Image version cannot be empty")
    private String imageVer;

    /**
     * The business types applicable to the image, render/ai
     */
    @NotBlank(message = "Business type cannot be empty")
    private String bizType;

    /**
     * Resource pool type applicable to the image: edge/idc
     */
    @NotBlank(message = "Resource pool type cannot be empty")
    private String resourcePool;

    /**
     * Operating system type: windows, linux, android
     */
    @NotBlank(message = "Operating system type cannot be empty")
    private String osType;

    /**
     * Container virtualization type: docker/kvm
     */
    @NotBlank(message = "Container type cannot be empty")
    private String contianerType;

    /**
     * Image storage path
     */
    @NotBlank(message = "Storage path cannot be empty")
    private String storagePath;

    /**
     * md5
     */
    @NotBlank(message = "md5 cannot be empty")
    private String md5;

    /**
     * Image download URL
     */
    @NotBlank(message = "Download URL cannot be empty")
    private String downloadUrl;

    /**
     * Custom label for the image
     */
    private String label;

    /**
     * Tenant ID of the image owner
     * If uploaded by administrator, tenantId is 10000
     */
    @NotNull(message = "Tenant ID cannot be empty")
    private Long tenantId;

    /**
     * Whether it is an official image
     * Default is false
     */
    private Boolean isOfficial = false;

    /**
     * Whether the image is enabled
     * Default is false when creating
     */
    private Boolean isEnabled = false;
}