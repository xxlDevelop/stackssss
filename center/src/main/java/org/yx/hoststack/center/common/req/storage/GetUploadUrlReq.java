package org.yx.hoststack.center.common.req.storage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GetUploadUrlReq {
    @NotBlank(message = "region cannot be empty")
    private String region;
    @NotBlank(message = "fileId cannot be empty")
    private String fileId;
}