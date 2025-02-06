package org.yx.hoststack.center.common.req.idc.net;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.yx.hoststack.center.common.utils.DataTypeUtil;
import org.yx.hoststack.protocol.ws.server.C2EMessage;

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

    @NotBlank(message = "Network protocol cannot be empty")
    @Pattern(regexp = "^(http|https|ws|wss|tcp|udp)$",
            message = "Protocol type must be one of: http/https/ws/wss/tcp/udp")
    private String netProtocol;

    @NotNull(message = "Inbound bandwidth limit cannot be empty")
    @Min(value = 0, message = "Bandwidth limit must be positive")
    @Max(value = 4294967295L, message = "Bandwidth limit exceeds maximum value for uint32")
    private Long bandwidthInLimit;

    @NotNull(message = "Outbound bandwidth limit cannot be empty")
    @Min(value = 0, message = "Bandwidth limit must be positive")
    @Max(value = 4294967295L, message = "Bandwidth limit exceeds maximum value for uint32")
    private Long bandwidthOutLimit;

    @NotBlank(message = "Network ISP type cannot be empty")
    private String netIspType;

    @NotBlank(message = "IP type cannot be empty")
    @Pattern(regexp = "^(ipv4|ipv6)$",
            message = "IP type must be either ipv4 or ipv6")
    private String ipType;

    @NotBlank(message = "Mapping name cannot be empty")
    private String mappingName;

    // 获取本地IP
    public String getLocalIp() {
        return localNet != null ? localNet.split(":")[0] : null;
    }

    // 获取本地端口
    public Integer getLocalPort() {
        return localNet != null ? Integer.parseInt(localNet.split(":")[1]) : null;
    }

    // 获取映射IP
    public String getMappingIp() {
        return mappingNet != null ? mappingNet.split(":")[0] : null;
    }

    // 获取映射端口
    public String getMappingPort() {
        return mappingNet != null ? mappingNet.split(":")[1] : null;
    }

    // 构建 EdgeNetConfig
    public C2EMessage.EdgeNetConfig toEdgeNetConfig() {
        return C2EMessage.EdgeNetConfig.newBuilder()
                .setLocalIp(getLocalIp())
                .setLocalPort(getLocalPort())
                .setMappingIp(getMappingIp())
                .setMappingPort(getMappingPort())
                .setNetProtocol(netProtocol)
                .setBandwidthInLimit(DataTypeUtil.convertToUInt32(bandwidthInLimit))
                .setBandwidthOutLimit(DataTypeUtil.convertToUInt32(bandwidthOutLimit))
                .setNetIspType(netIspType)
                .setIpType(ipType)
                .build();
    }

}