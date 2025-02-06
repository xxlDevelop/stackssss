package org.yx.hoststack.center.common.req.storage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GetDownloadUrlReq {
    @NotBlank(message = "region is required")
    private String region;

    @NotBlank(message = "fileId is required")
    private String fileId;
}