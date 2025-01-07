package org.yx.hoststack.center.common.config.virtual;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.yx.hoststack.center.common.properties.ApplicationsVirtualThreadProperties;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import static org.yx.hoststack.center.common.constant.CenterEvent.Action.TASK_REJECTED;
import static org.yx.hoststack.center.common.constant.CenterEvent.THREAD_TASK_REJECTED_EVENT;
import static org.yx.hoststack.center.common.enums.SysCode.x00000600;

/**
 * @Description : Virtual Thread config
 * @Author : Lee666
 * @Date : 2025/1/6
 * @Version : 1.0
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ApplicationsVirtualThreadProperties.class)
public class VirtualThreadConfig {
    private final ApplicationsVirtualThreadProperties applicationsVirtualThreadProperties;

    public VirtualThreadConfig(ApplicationsVirtualThreadProperties applicationsVirtualThreadProperties) {
        this.applicationsVirtualThreadProperties = applicationsVirtualThreadProperties;
    }

    @Bean
    @RefreshScope
    public AsyncTaskExecutor asyncTaskExecutor() {
        // Create a ThreadPoolTaskExecutor with core thread count and maximum thread count
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setVirtualThreads(applicationsVirtualThreadProperties.getPool().getEnable());
        // Number of core threads
        executor.setCorePoolSize(applicationsVirtualThreadProperties.getPool().getCoreSize());
        // Maximum number of threads
        executor.setMaxPoolSize(applicationsVirtualThreadProperties.getPool().getMaxSize());
        // Queue capacity
        executor.setQueueCapacity(applicationsVirtualThreadProperties.getPool().getQueueCapacity());
        executor.setThreadNamePrefix(applicationsVirtualThreadProperties.getPool().getNamePrefix());
        executor.setRejectedExecutionHandler((r, e) -> {
            int activeCount = executor.getActiveCount();
            int queueSize = executor.getThreadPoolExecutor().getQueue().size();
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, THREAD_TASK_REJECTED_EVENT)
                    .p(LogFieldConstants.ACTION, TASK_REJECTED)
                    .p(LogFieldConstants.ERR_CODE, x00000600.getValue())
                    .p(LogFieldConstants.Code, x00000600.getValue())
                    .p(LogFieldConstants.ERR_MSG, x00000600.getMsg())
                    .p("TaskDescription", r.toString())
                    .p("ActiveThreads", activeCount)
                    .p("QueueSize", queueSize)
                    .p(LogFieldConstants.Alarm, 0)
                    .w();
        });
        executor.initialize();

        // Packaging the standard thread pool into a thread pool that supports virtual threads
        return new TaskExecutorAdapter(executor);
    }

}
