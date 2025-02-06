package org.yx.hoststack.center.common.req.storage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FileDistributeReq {
    private String region;
    private String idc;
    
    @NotBlank(message = "objectKey is required")
    private String fileId;
    
    @NotBlank(message = "bucket is required")
    private String bucket;
    
    @NotBlank(message = "downloadUrl is required")
    private String downloadUrl;
    
    @NotBlank(message = "md5 is required")
    private String md5;
}