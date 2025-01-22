package org.yx.hoststack.center.common.req.image;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ImageStatusReq {
    @NotBlank(message = "The image ID cannot be empty")
    private String imageId;

    @NotNull(message = "The available status cannot be empty")
    private Boolean isEnabled;
}