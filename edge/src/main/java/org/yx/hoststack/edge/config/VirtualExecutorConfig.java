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
    @Value("${spring.threads.virtual.warmUp:false}")
    private boolean needWarmUp;

    @Value("${spring.threads.virtual.warmUpThread:10000}")
    private int warmUpThread;

    private static final String TASK_NAME = "v-executor-";

    @Bean("edgeExecutor")
    public Executor edgeVirtualExecutor() {
        ThreadFactory factory = Thread.ofVirtual().name(TASK_NAME, 0).factory();
        ExecutorService executorService = Executors.newThreadPerTaskExecutor(factory);
        if (needWarmUp) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, "EdgeVirtualExecutor")
                    .p(LogFieldConstants.ACTION, "WarmUpStart")
                    .p("WarmUpCount", warmUpThread)
                    .i();
            long start = System.currentTimeMillis();
            IntStream.range(0, warmUpThread).forEach(i -> executorService.execute(() -> {
            }));
            try {
                executorService.awaitTermination(60, TimeUnit.SECONDS);
            } catch (Exception ignored) {
            }
            long end = System.currentTimeMillis();
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, "EdgeVirtualExecutor")
                    .p(LogFieldConstants.ACTION, "WarmUpFinish")
                    .p("UsedMs", (end - start))
                    .i();
        }
        return executorService;
    }
}
