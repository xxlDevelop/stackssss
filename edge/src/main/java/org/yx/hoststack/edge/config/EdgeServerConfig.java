package org.yx.hoststack.edge.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class EdgeServerConfig {
    @Value("${server.wsPort}")
    private Integer wsPort;
    @Value("${server.bossThreadCount:1}")
    private int bossThreadCount;
    @Value("${server.workThreadCount:3}")
    private int workThreadCount;
    @Value("${server.soBacklog:1024}")
    private int backlog;
    @Value("${server.recBuf:524288}")//512KB
    private int recBuf;
    @Value("${server.sendBuf:524288}")//512KB
    private int sendBuf;
    @Value("${server.enableIdle:false}")
    private boolean enableIdle;
    @Value("${server.readIdle:60}")
    private int readIdle;
    @Value("${server.writeIdle:60}")
    private int writeIdle;
    @Value("${server.allIdle:120}")
    private int allIdle;
    @Value("${server.retryNumber:3}")
    private int retryNumber;
}
