package org.yx.hoststack.center.common.req.host;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.yx.hoststack.center.common.req.PageReq;

/**
 *
 * packageName org.yx.hoststack.center.common.req.host
 * @author YI-JIAN-ZHANG
 * @version JDK 8
 * @className HostReq
 * @date 2025/1/23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HostReq extends PageReq {
    private Long tenantId;

    private String idc;

    private String region;

    private String hostIds;
}
