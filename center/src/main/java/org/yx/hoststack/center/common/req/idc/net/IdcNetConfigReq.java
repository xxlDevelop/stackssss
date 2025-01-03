package org.yx.hoststack.center.common.req.idc.net;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * IDC Network Configuration Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdcNetConfigReq {

    @NotBlank(message = "IDC identifier cannot be empty")
    private String idc;

    @NotBlank(message = "Internal network address cannot be empty")
    @Pattern(regexp = "^([0-9]{1,3}\\.){3}[0-9]{1,3}:[0-9]{1,5}$",
            message = "Invalid internal network address format, should be IP:Port")
    private String localNet;

    @NotBlank(message = "External network address cannot be empty")
    @Pattern(regexp = "^([0-9]{1,3}\\.){3}[0-9]{1,3}:[0-9]{1,5}$",
            message = "Invalid external network address format, should be IP:Port")
    private String mappingNet;

    private String mask;

    private String gateway;

    private String dns1;

    private String dns2;

    @NotBlank(message = "Network protocol cannot be empty")
    @Pattern(regexp = "^(http|https|ws|wss|tcp|udp)$",
            message = "Protocol type must be one of: http/https/ws/wss/tcp/udp")
    private String netProtocol;

    @NotNull(message = "Inbound bandwidth limit cannot be empty")
    private Long bandwidthInLimit;

    @NotNull(message = "Outbound bandwidth limit cannot be empty")
    private Long bandwidthOutLimit;

    @NotBlank(message = "Network ISP type cannot be empty")
    private String netIspType;

    @NotBlank(message = "IP type cannot be empty")
    @Pattern(regexp = "^(ipv4|ipv6)$",
            message = "IP type must be either ipv4 or ipv6")
    private String ipType;

    @NotBlank(message = "Mapping name cannot be empty")
    private String mappingName;
}