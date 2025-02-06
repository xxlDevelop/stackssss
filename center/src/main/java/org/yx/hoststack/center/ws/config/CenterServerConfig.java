package org.yx.hoststack.center.ws.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class CenterServerConfig {
    @Value("${applications.applications.server.wsPort:8801}")
    private Integer wsPort;
    @Value("${applications.applications.server.bossThreadCount:1}")
    private int bossThreadCount;
    @Value("${applications.applications.server.workThreadCount:3}")
    private int workThreadCount;
    @Value("${applications.server.soBacklog:1024}")
    private int backlog;
    @Value("${applications.server.recBuf:524288}")//512KB
    private int recBuf;
    @Value("${applications.server.sendBuf:524288}")//512KB
    private int sendBuf;
    @Value("${applications.server.logLevel:2}")
    private int logLevel;
    @Value("${applications.server.enableIdle:false}")
    private boolean enableIdle;
    @Value("${applications.server.readIdle:60}")
    private int readIdle;
    @Value("${applications.server.writeIdle:60}")
    private int writeIdle;
    @Value("${applications.server.allIdle:120}")
    private int allIdle;
    @Value("${applications.server.retryNumber:3}")
    private int retryNumber;
}
