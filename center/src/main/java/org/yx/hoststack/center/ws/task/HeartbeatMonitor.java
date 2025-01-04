package org.yx.hoststack.center.ws.task;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.yx.hoststack.center.CenterApplicationRunner;
import org.yx.hoststack.center.common.enums.RegisterNodeEnum;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

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
                HeartbeatTask task = queue.take();
                System.out.println("Task taken: " + JSONObject.toJSONString(task)); // Debug log

                task.executeTimeoutCallback();
                CenterApplicationRunner.centerNode.printNodeInfo(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // 更新心跳：如果服务ID已经存在，则移除旧任务并重新加入队列
    public void updateHeartbeat(String serviceId, RegisterNodeEnum type, Runnable timeoutCallback) {
        lock.lock();
        try {
            HeartbeatTask task = new HeartbeatTask(serviceId, 10, type, timeoutCallback);
            System.out.println("Offering task to queue: " + JSONObject.toJSONString(task)); // Debug log

            if (RegisterNodeEnum.HOST.equals(type)) {
                containerQueue.offer(new HeartbeatTask(serviceId,  3, type, timeoutCallback));
            } else {
                serverQueue.offer(task);
            }
        } finally {
            lock.unlock();
        }
    }
}
