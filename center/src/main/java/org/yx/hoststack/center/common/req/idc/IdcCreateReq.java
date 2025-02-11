package org.yx.hoststack.center.common.req.idc;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IdcCreateReq {

    @NotBlank
    private String zone;
    @NotBlank
    private String region;
    @NotBlank
    private String idcIp;
    @NotBlank
    private String idc;
    @NotBlank
    private String localHsIdcHttpSvc;
    @NotBlank
    private String netHsIdcHttpsSvc;
    @NotBlank
    private String localHsIdcWsSvc;
    @NotBlank
    private String localShareStorageHttpSvc;
    @NotBlank
    private String shareStorageUser;
    @NotBlank
    private String shareStoragePwd;
    @NotBlank
    private String localLogSvcHttpSvc;
    @NotBlank
    private String netLogSvcHttpsSvc;
    @NotBlank
    private String speedTestSvc;
    @NotBlank
    private String location;
}
