package org.yx.hoststack.center.common.resp.relay;

import lombok.*;
import org.yx.hoststack.center.entity.RelayInfo;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelayListResp {
    /**
     * Zone identifier
     */
    private String zone;
    
    /**
     * Region identifier
     */
    private String region;
    
    /**
     * Relay node identifier
     */
    private String relay;
    
    /**
     * Public IP address of relay forwarding node
     */
    private String relayIp;
    
    /**
     * Public HTTPS base address
     */
    private String netHttpsSvc;
    
    /**
     * Public WSS base address
     */
    private String netWssSvc;
    
    /**
     * Relay node GPS coordinates
     */
    private String location;


    public RelayListResp(RelayInfo relayInfo) {
        this.zone = relayInfo.getZone();
        this.region = relayInfo.getRegion();
        this.relay = relayInfo.getRelay();
        this.relayIp = relayInfo.getRelayIp();
        this.netHttpsSvc = relayInfo.getNetHttpsSvc();
        this.netWssSvc = relayInfo.getNetWssSvc();
        this.location = relayInfo.getLocation();
    }
}