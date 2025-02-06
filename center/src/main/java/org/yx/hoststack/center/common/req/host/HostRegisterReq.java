package org.yx.hoststack.center.common.req.host;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class HostRegisterReq {
    private Long tenantId;

    private String idc;
    
    private String region;

    @NotBlank(message = "hostIds be not null")
    private String hostIds;
}
