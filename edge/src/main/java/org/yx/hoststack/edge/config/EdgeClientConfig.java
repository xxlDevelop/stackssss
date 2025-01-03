package org.yx.hoststack.edge.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class EdgeClientConfig {
    @Value("${webSocket.client.soBacklog:1024}")
    private int backlog;
    @Value("${webSocket.client.recBuf:524288}")//512KB
    private int recBuf;
    @Value("${webSocket.client.sendBuf:524288}")//512KB
    private int sendBuf;
    @Value("${webSocket.client.retryNumber:3}")
    private int retryNumber;
    @Value("${webSocket.client.connectTimeout:5}")
    private int connectTimeout;
}
