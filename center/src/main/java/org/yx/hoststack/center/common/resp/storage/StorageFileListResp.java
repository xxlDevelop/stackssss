package org.yx.hoststack.center.common.resp.storage;

import lombok.Builder;
import lombok.Data;
import org.yx.hoststack.center.entity.StorageFile;
import org.yx.hoststack.center.entity.StorageBucket;

@Data
@Builder
public class StorageFileListResp {

    private String fileId;
    private String region;
    private String idc;
    private String bucket;
    private String objectKey;
    private String md5;
    private Long size;
    private String localDownloadUrl;
    private String netDownloadUrl;
    public static StorageFileListResp from(StorageFile file, StorageBucket bucket) {
        return StorageFileListResp.builder()
                .fileId(file.getFileId())
                .region(bucket.getRegion())
                .idc(bucket.getIdc())
                .bucket(bucket.getBucket())
                .objectKey(file.getObjectKey())
                .md5(file.getMd5())
                .size(file.getSize())
                .localDownloadUrl(file.getLocalDownloadUrl())
                .netDownloadUrl(file.getNetDownloadUrl())
                .build();
    }
}