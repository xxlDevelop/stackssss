package org.yx.hoststack.center.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.yx.hoststack.center.common.deserializer.NonEmptyStringDeserializer;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OssConfigDetail {
    private static final int DEFAULT_TOKEN_DURATION = 14400; // 4 hours
    private static final String DEFAULT_RESOURCE_ACTIONS = "GetObject,GetObjectAcl,PutObject,SetObjectAcl,ListMultipartUploadParts,AbortMultipartUpload,ListObjects";
    private static final String DEFAULT_BUCKET_ACTIONS = "ListObjects,ListMultipartUploadParts,ListBucketMultipartUploads";

    private String bucket;
    private String fileId;
    private String region;
    private String accessKey;
    private String secretKey;
    private String endpoint;
    private String roleARN;

    @JsonProperty("tokenDurationSeconds")
    @JsonDeserialize(using = NonEmptyStringDeserializer.class)
    private Integer tokenDurationSeconds = DEFAULT_TOKEN_DURATION;
    @JsonProperty("resourceActions")
    @JsonDeserialize(using = NonEmptyStringDeserializer.class)
    private String defaultResourceActions = DEFAULT_RESOURCE_ACTIONS;
    @JsonProperty("bucketActions")
    @JsonDeserialize(using = NonEmptyStringDeserializer.class)
    private String defaultBucketActions = DEFAULT_BUCKET_ACTIONS;


}