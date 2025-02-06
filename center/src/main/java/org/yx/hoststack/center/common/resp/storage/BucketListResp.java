package org.yx.hoststack.center.common.resp.storage;

import lombok.*;
import org.yx.hoststack.center.entity.StorageBucket;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BucketListResp {
    private String region;
    private String idc;
    private String bucket;

    /**
     * Convert StorageBucket entity to BucketListResp
     *
     * @param bucket storage bucket entity
     * @return bucket list response
     */
    public static BucketListResp from(StorageBucket bucket) {
        if (bucket == null) {
            return null;
        }

        return BucketListResp.builder()
                .region(bucket.getRegion())
                .idc(bucket.getIdc())
                .bucket(bucket.getBucket())
                .build();
    }
}
