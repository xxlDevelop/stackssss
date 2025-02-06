package org.yx.hoststack.center.common.dto;

import com.amazonaws.services.s3.AmazonS3;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class S3ClientWrapper {
    private AmazonS3 s3Client;
    private OssConfigDetail configDetail;
}