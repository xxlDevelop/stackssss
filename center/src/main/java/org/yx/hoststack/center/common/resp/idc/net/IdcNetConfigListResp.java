package org.yx.hoststack.center.common.resp.idc.net;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IDC Network Configuration List Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdcNetConfigListResp {

    private String localNet;        // Internal network address (IP:Port)
    private String mappingNet;      // External network address (IP:Port)
    private String mask;            // Subnet mask
    private String gateway;         // Gateway
    private String dns1;            // DNS1
    private String dns2;            // DNS2
    private String netProtocol;     // Network protocol
    private Long bandwidthInLimit;  // Inbound bandwidth limit
    private Long bandwidthOutLimit; // Outbound bandwidth limit
    private String netIspType;      // Network ISP type
    private String ipType;          // IP type
    private String mappingName;     // Mapping name
}