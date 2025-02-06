package org.yx.hoststack.center.common.resp.storage;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetUploadUrlRequestResp {
    private String uploadUrl;
}
