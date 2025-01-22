package org.yx.hoststack.center.common.req.image;

import lombok.Data;
import org.yx.hoststack.center.common.req.PageReq;

@Data
public class ImageListReq extends PageReq {
    private String bizType;
    private String resourcePool;
    private String osType;
    private String contianerType;
    private String label;
    private Long tenantId;
    private Boolean isOfficial;
    private Boolean isEnabled;
}