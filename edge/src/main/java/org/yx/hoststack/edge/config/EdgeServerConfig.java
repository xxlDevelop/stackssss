package org.yx.hoststack.edge.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class EdgeServerConfig {
    @Value("${webSocket.server.wsPort}")
    private Integer wsPort;
    @Value("${webSocket.server.bossThreadCount:1}")
    private int bossThreadCount;
    @Value("${webSocket.server.workThreadCount:3}")
    private int workThreadCount;
    @Value("${webSocket.server.soBacklog:1024}")
    private int backlog;
    @Value("${webSocket.server.recBuf:524288}")//512KB
    private int recBuf;
    @Value("${webSocket.server.sendBuf:524288}")//512KB
    private int sendBuf;
    @Value("${webSocket.server.enableIdle:false}")
    private boolean enableIdle;
    @Value("${webSocket.server.readIdle:60}")
    private int readIdle;
    @Value("${webSocket.server.writeIdle:60}")
    private int writeIdle;
    @Value("${webSocket.server.allIdle:120}")
    private int allIdle;
    @Value("${webSocket.server.retryNumber:3}")
    private int retryNumber;
}
