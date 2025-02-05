package org.yx.hoststack.edge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.concurrent.*;
import java.util.stream.IntStream;

@Configuration
@ConditionalOnProperty(value = "spring.threads.virtual.enabled", havingValue = "true")
public class VirtualExecutorConfig {
    @Value("${spring.threads.virtual.needWarmUp:false}")
    private boolean needWarmUp;

    @Value("${spring.threads.virtual.warmUpThread:10000}")
    private int warmUpThread;


    private static final String TASK_NAME = "edge-v-executor-";

    @Bean("edgeExecutor")
    public Executor edgeVirtualExecutor() throws InterruptedException {
        ThreadFactory factory = Thread.ofVirtual().name(TASK_NAME).factory();
        ExecutorService executor = Executors.newThreadPerTaskExecutor(factory);
        if (needWarmUp) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, "EdgeVirtualExecutor")
                    .p(LogFieldConstants.ACTION, "WarmUpStart")
                    .p("WarmUpCount", warmUpThread)
                    .i();
            long start = System.currentTimeMillis();
            IntStream.range(0, warmUpThread).forEach(i -> executor.execute(() -> {
            }));
            executor.awaitTermination(60, TimeUnit.SECONDS);
            long end = System.currentTimeMillis();
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, "EdgeVirtualExecutor")
                    .p(LogFieldConstants.ACTION, "WarmUpFinish")
                    .p("UsedMs", (end - start))
                    .i();
        }
        return executor;
    }
}
