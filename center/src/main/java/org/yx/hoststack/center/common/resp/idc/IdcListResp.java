package org.yx.hoststack.center.common.resp.idc;

import lombok.Getter;
import lombok.Setter;
import org.yx.hoststack.center.entity.IdcInfo;

@Getter
@Setter
public class IdcListResp {

    private String zone;
    private String region;
    private String idcIp;
    private String localHsIdcHttpSvc;
    private String netHsIdcHttpsSvc;
    private String localHsIdcWsSvc;
    private String localShareStorageHttpSvc;
    private String shareStorageUser;
    private String shareStoragePwd;
    private String localLogSvcHttpSvc;
    private String netLogSvcHttpsSvc;
    private String speedTestSvc;
    private String location;

    public IdcListResp(IdcInfo idcInfo) {
        this.zone = idcInfo.getZone();
        this.region = idcInfo.getRegion();
        this.idcIp = idcInfo.getIdcIp();
        this.localHsIdcHttpSvc = idcInfo.getLocalHsIdcHttpSvc();
        this.netHsIdcHttpsSvc = idcInfo.getNetHsIdcHttpsSvc();
        this.localHsIdcWsSvc = idcInfo.getLocalHsIdcWsSvc();
        this.localShareStorageHttpSvc = idcInfo.getLocalShareStorageHttpSvc();
        this.shareStorageUser = idcInfo.getShareStorageUser();
        this.shareStoragePwd = idcInfo.getShareStoragePwd();
        this.localLogSvcHttpSvc = idcInfo.getLocalLogSvcHttpSvc();
        this.netLogSvcHttpsSvc = idcInfo.getNetLogSvcHttpsSvc();
        this.speedTestSvc = idcInfo.getSpeedTestSvc();
        this.location = idcInfo.getLocation();
    }
}
