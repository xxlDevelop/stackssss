package org.yx.hoststack.edge;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.yx.hoststack.edge.config.EdgeCommonConfig;
import org.yx.hoststack.edge.queue.consumers.HostExitConsumer;
import org.yx.hoststack.edge.queue.consumers.HostHbConsumer;
import org.yx.hoststack.edge.queue.consumers.JobNotifyToCenterConsumer;
import org.yx.hoststack.edge.queue.consumers.JobNotifyToLocalDiskConsumer;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EdgeApplicationRunner implements ApplicationRunner {

    private final HostHbConsumer hostHbConsumer;
    private final JobNotifyToLocalDiskConsumer jobNotifyToLocalDiskConsumer;
    private final JobNotifyToCenterConsumer jobNotifyToCenterConsumer;
    private final HostExitConsumer hostExitConsumer;
    private final EdgeCommonConfig edgeCommonConfig;

    private final ExecutorService worker = Executors.newFixedThreadPool(4, ThreadFactoryBuilder.create().setNamePrefix("edge-worker-").build());

    @Override
    public void run(ApplicationArguments args) {
        worker.execute(hostHbConsumer);
        worker.execute(jobNotifyToLocalDiskConsumer);
        worker.execute(jobNotifyToCenterConsumer);
        worker.execute(hostExitConsumer);

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
