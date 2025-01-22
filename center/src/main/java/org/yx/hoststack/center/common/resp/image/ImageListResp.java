package org.yx.hoststack.center.common.resp.image;

import lombok.Data;
import org.yx.hoststack.center.entity.ImageInfo;

import java.sql.Timestamp;

@Data
public class ImageListResp {
    private String imageId;
    private String imageName;
    private String imageVer;
    private String bizType;
    private String resourcePool;
    private String osType;
    private String contianerType;
    private String storagePath;
    private String downloadUrl;
    private String label;
    private Long tenantId;
    private Boolean isOfficial;
    private Boolean isEnabled;
    private Timestamp createAt;

    public ImageListResp(ImageInfo imageInfo) {
        this.imageId = imageInfo.getImageId();
        this.imageName = imageInfo.getImageName();
        this.imageVer = imageInfo.getImageVer();
        this.bizType = imageInfo.getBizType();
        this.resourcePool = imageInfo.getResourcePool();
        this.osType = imageInfo.getOsType();
        this.contianerType = imageInfo.getContianerType();
        this.storagePath = imageInfo.getStoragePath();
        this.downloadUrl = imageInfo.getDownloadUrl();
        this.label = imageInfo.getLabel();
        this.tenantId = imageInfo.getTenantId();
        this.isOfficial = imageInfo.getIsOfficial();
        this.isEnabled = imageInfo.getIsEnabled();
        this.createAt = (Timestamp) imageInfo.getCreateAt();
    }


}