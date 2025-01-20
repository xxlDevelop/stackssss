package org.yx.hoststack.center.common.config.channel;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class ChannelSendConfig {
    @Value("${server.center.sendMsgUrl:/hs-core/v1/center/channel}")
    private String sendMsgUrl;
    @Value("${webSocket.client.retryNumber:3}")
    private int retryNumber;
    @Value("${webSocket.client.retryInterval:10}")
    private int retryInterval;
}
