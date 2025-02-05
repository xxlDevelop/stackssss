package org.yx.hoststack.center.common.req.idc.net;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IdcNetConfigReq {

    @NotBlank
    private String idc;
    @NotBlank
    private String localNet;
    @NotBlank
    private String mappingNet;
    private String mask;
    private String gateway;
    private String dns1;
    private String dns2;
    @NotBlank
    private String netProtocol;
    @NotNull
    private Long bandwidthInLimit;
    @NotNull
    private Long bandwidthOutLimit;
    @NotBlank
    private String netIspType;
    @NotBlank
    private String ipType;
    @NotBlank
    private String mappingName;
}
