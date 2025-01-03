package org.yx.hoststack.edge.cache.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IdcNetConfig {
    private String localIp;
    private int localPort;
    private String mappingIp;
    private String mappingPort;
    private String netProtocol;
    private int bandwidthInLimit;
    private int bandwidthOutLimit;
    private String netIspType;
    private String ipType;
}
