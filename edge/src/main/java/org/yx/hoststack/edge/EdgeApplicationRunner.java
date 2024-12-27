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
import org.yx.hoststack.edge.queue.consumers.JobNotifyToLocalDiskConsumer;
import org.yx.hoststack.edge.queue.consumers.JobNotifyToCenterConsumer;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.hoststack.protocol.ws.agent.common.MessageType;
import org.yx.lib.utils.util.SpringContextHolder;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class EdgeApplicationRunner implements org.springframework.boot.ApplicationRunner {

    private final @Qualifier("edgeExecutor") Executor executor;
    private final HostHbConsumer hostHbConsumer;
    private final JobNotifyToLocalDiskConsumer jobNotifyToLocalDiskConsumer;
    private final JobNotifyToCenterConsumer jobNotifyToCenterConsumer;
    private final EdgeCommonConfig edgeCommonConfig;

    @Override
    public void run(ApplicationArguments args) {
        executor.execute(hostHbConsumer);
        executor.execute(jobNotifyToLocalDiskConsumer);
        executor.execute(jobNotifyToCenterConsumer);

        File notSendJobNotifySavePath = new File(edgeCommonConfig.getNotSendJobNotifySavePath());
        if (!notSendJobNotifySavePath.exists()) {
            notSendJobNotifySavePath.mkdirs();
        }

        // test
//        executor.execute(() -> {
//            MessageQueues messageQueues = SpringContextHolder.getBean(MessageQueues.class);
//            for (int i = 0; i < 900000; i++) {
//                messageQueues.getJobNotifyToDiskQueue().add(AgentCommonMessage.builder()
//                        .method("CreateVM")
//                        .hostId(UUID.fastUUID().toString())
//                        .type(MessageType.NOTIFY)
//                        .traceId(UUID.fastUUID().toString())
//                        .jobId(UUID.fastUUID() + "-" + "hostId")
//                        .progress(100)
//                        .status("success")
//                        .code(0)
//                        .build());
//                if (i % 100 == 0) {
//                    try {
//                        TimeUnit.MILLISECONDS.sleep(50);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }
//        });
    }
}
