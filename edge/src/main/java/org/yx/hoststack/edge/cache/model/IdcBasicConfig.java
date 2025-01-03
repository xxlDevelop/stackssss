package org.yx.hoststack.edge.cache.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IdcBasicConfig {
    public final static String LOCAL_SHARE_STORAGE_HTTP_SVC = "LOCAL_SHARE_STORAGE_HTTP_SVC";
    public final static String SHARE_STORAGE_USER = "SHARE_STORAGE_USER";
    public final static String SHARE_STORAGE_PWD = "SHARE_STORAGE_PWD";
    public final static String LOCAL_LOG_SVC_HTTP_SVC = "LOCAL_LOG_SVC_HTTP_SVC";
    public final static String NET_LOG_SVC_HTTPS_SVC = "NET_LOG_SVC_HTTPS_SVC";
    public final static String SPEED_TEST_SVC = "SPEED_TEST_SVC";
    public final static String LOCATION = "LOCATION";

    private String localShareStorageHttpSvc;
    private String shareStorageUser;
    private String shareStoragePwd;
    private String localLogSvcHttpSvc;
    private String netLogSvcHttpsSvc;
    private String speedTestSvc;
    private String location;
}
