package org.yx.hoststack.center.ws.heartbeat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.yx.hoststack.center.common.enums.RegisterNodeEnum;
import org.yx.hoststack.center.entity.ServiceDetail;
import org.yx.hoststack.center.ws.controller.EdgeController;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.SpringContextHolder;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.yx.hoststack.center.ws.controller.EdgeController.serverDetailCacheMap;

@Component
@RefreshScope
public class HeartbeatMonitor {
    @Value("${applications.serverHbInterval}")
    private Integer serverHbInterval;
    @Value("${applications.hostHbInterval}")
    private Integer hostHbInterval;

    private final DelayQueue<HeartbeatTask> containerQueue = new DelayQueue<>();
    private final DelayQueue<HeartbeatTask> serverQueue = new DelayQueue<>();

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

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
                    TimeUnit.MILLISECONDS.sleep(50);
                } else {
                    RegisterNodeEnum registerNodeEnum = task.getType();
                    String serviceId = task.getServiceId();
                    long time = 0L;
                    switch (registerNodeEnum) {
                        case RELAY, IDC -> {
                            ServiceDetail detail = serverDetailCacheMap.get(serviceId);
                            if (!ObjectUtils.isEmpty(detail)) {
                                time = detail.getLastHbAt().getTime();
                                if (time > task.getExpirationTime()) {
                                    return;
                                }
                            }
                        }
                    }
                    task.executeTimeoutCallback();
                    KvLogger.instance(SpringContextHolder.getBean(EdgeController.class)).p(LogFieldConstants.ACTION, String.format("%s:ServiceID:%s-HeatBeat Timeout", task.getType(), serviceId))
                            .p("expirationTime", task.getExpirationTime())
                            .p("currentTime", time)
                            .i();
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
            containerQueue.offer(new HeartbeatTask(serviceId, (hostHbInterval * 2) + 1, type, timeoutCallback));
        } else {

            serverQueue.removeIf(task -> serviceId.equals(task.getServiceId()));

            serverQueue.offer(new HeartbeatTask(serviceId, (serverHbInterval * 2) + 1, type, timeoutCallback));
            System.out.println(serverQueue.size());
        }
    }
}
