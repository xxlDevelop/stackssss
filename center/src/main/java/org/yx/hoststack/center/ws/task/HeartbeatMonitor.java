package org.yx.hoststack.center.ws.task;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.yx.hoststack.center.common.enums.RegisterNodeEnum;
import org.yx.hoststack.center.ws.CenterServer;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Component
@RefreshScope
public class HeartbeatMonitor {
    @Value("${applications.serverHbInterval}")
    private Integer serverHbInterval;
    @Value("${applications.hostHbInterval}")
    private Integer hostHbInterval;

    private final DelayQueue<HeartbeatTask> containerQueue = new DelayQueue<>();  // 容器心跳队列
    private final DelayQueue<HeartbeatTask> serverQueue = new DelayQueue<>();  // 服务器心跳队列

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private final ReentrantLock lock = new ReentrantLock();  // 锁来确保线程安全

    public void startMonitor() {
        executor.submit(() -> processQueue(containerQueue));
        executor.submit(() -> processQueue(serverQueue));
    }

    // 处理超时任务队列
    private void processQueue(DelayQueue<HeartbeatTask> queue) {
        while (true) {
            try {
                HeartbeatTask task = queue.poll();
                if (ObjectUtils.isEmpty(task)) {
                    TimeUnit.SECONDS.sleep(NumberUtils.INTEGER_ONE);
                } else {
                    task.executeTimeoutCallback();
                }
            } catch (InterruptedException e) {
                KvLogger.instance(this).p(LogFieldConstants.ACTION, "Interrupted")
                        .p("HeartbeatMonitor throw Exception Interrupted", e.getMessage()).e();
            }
        }
    }

    // 更新心跳：如果服务ID已经存在，则移除旧任务并重新加入队列
    public void updateHeartbeat(String serviceId, RegisterNodeEnum type, Consumer<Long> timeoutCallback) {
        if (RegisterNodeEnum.HOST.equals(type)) {
            containerQueue.offer(new HeartbeatTask(serviceId, hostHbInterval=1, type, timeoutCallback));
        } else {

            serverQueue.removeIf(task -> serviceId.equals(task.getServiceId()));

            serverQueue.offer(new HeartbeatTask(serviceId, serverHbInterval +1000, type, timeoutCallback));
            System.out.println(serverQueue.size());
        }
    }
}
