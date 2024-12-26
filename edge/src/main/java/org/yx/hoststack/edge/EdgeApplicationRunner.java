package org.yx.hoststack.edge;

import cn.hutool.core.lang.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;
import org.yx.hoststack.edge.config.EdgeCommonConfig;
import org.yx.hoststack.edge.queue.MessageQueues;
import org.yx.hoststack.edge.queue.consumers.HostHbConsumer;
import org.yx.hoststack.edge.queue.consumers.NotSendJobNotifyConsumer;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.lib.utils.util.SpringContextHolder;

import java.io.File;
import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class EdgeApplicationRunner implements org.springframework.boot.ApplicationRunner {

    private final @Qualifier("edgeExecutor") Executor executor;
    private final HostHbConsumer hostHbConsumer;
    private final NotSendJobNotifyConsumer notSendJobNotifyConsumer;
    private final EdgeCommonConfig edgeCommonConfig;

    @Override
    public void run(ApplicationArguments args) {
        executor.execute(hostHbConsumer);
        executor.execute(notSendJobNotifyConsumer);

        File notSendJobNotifySavePath = new File(edgeCommonConfig.getNotSendJobNotifySavePath());
        if (!notSendJobNotifySavePath.exists()) {
            notSendJobNotifySavePath.mkdirs();
        }
//        executor.execute(() -> {
//            MessageQueues messageQueues = SpringContextHolder.getBean(MessageQueues.class);
//            for (int i = 0; i < 10000; i++) {
//                messageQueues.getJobNotifyNotSendQueue().add(AgentCommonMessage.builder()
//                        .hostId(UUID.fastUUID().toString())
//                        .build());
//            }
//        });
    }
}
