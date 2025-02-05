package org.yx.hoststack.edge.config;

import io.netty.util.NettyRuntime;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@Data
@ConditionalOnProperty(value = "spring.threads.virtual.enabled", havingValue = "false")
public class ExecutorConfig {

    @Value("${executor.pool.core-size:5}")
    private int corePoolSize;

    public int getCorePoolSize() {
        if (corePoolSize <= 3) {
            corePoolSize = NettyRuntime.availableProcessors();
        }
        return corePoolSize;
    }

    @Value("${executor.max-size:15}")
    private int maxPoolSize;

    public int getMaxPoolSize() {
        if (maxPoolSize <= 5) {
            maxPoolSize = NettyRuntime.availableProcessors();
        }
        return maxPoolSize;
    }

    @Value("${executor.capacity:2048}")
    private int queueCapacity;

    public int getQueueCapacity() {
        if (queueCapacity < 1024) {
            queueCapacity = 1024;
        }
        return queueCapacity;
    }

    /**
     * 允许的空闲时间
     */
    @Value("${executor.keep-alive:60}")
    private int keepAlive;

    public int getKeepAlive() {
        if (keepAlive < 0) {
            keepAlive = 30;
        }
        return keepAlive;
    }


    private static final String TASK_NAME = "edge-executor-";

    @Bean("edgeExecutor")
    public Executor edgeExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(getCorePoolSize() * 2);
        executor.setMaxPoolSize(getMaxPoolSize() * 3 + 1);
        executor.setQueueCapacity(getQueueCapacity());
        executor.setThreadNamePrefix(TASK_NAME);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(getKeepAlive());
        executor.initialize();
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }
}
