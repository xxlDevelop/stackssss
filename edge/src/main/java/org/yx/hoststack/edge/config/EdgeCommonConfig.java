package org.yx.hoststack.edge.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class EdgeCommonConfig {
    @Value("${server.port:8080}")
    private Integer serverPort;

    @Value("${runMode}")
    private String runMode;

    @Value("${localIp}")
    private String localIp;

    @Value("${notSendJobNotifySavePath:/data/host-stack/edge/notify}")
    private String notSendJobNotifySavePath;

    @Value("${notSendJobNotifyFileMaxSize:2}")
    private int notSendJobNotifyFileMaxSize;

    @Value("${adminTid:10000}")
    private Integer adminTid;
}
