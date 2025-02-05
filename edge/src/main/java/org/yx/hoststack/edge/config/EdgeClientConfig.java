package org.yx.hoststack.edge.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class EdgeClientConfig {
    @Value("${client.soBacklog:1024}")
    private int backlog;
    @Value("${client.recBuf:524288}")//512KB
    private int recBuf;
    @Value("${client.sendBuf:524288}")//512KB
    private int sendBuf;
    @Value("${client.retryNumber:3}")
    private int retryNumber;
    @Value("${client.connectTimeout:5}")
    private int connectTimeout;
}
