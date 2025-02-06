package org.yx.hoststack.center.common.resp.storage;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetDownloadUrlResp {
    private String downloadUrl;
}