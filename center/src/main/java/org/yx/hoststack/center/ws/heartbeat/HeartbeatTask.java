package org.yx.hoststack.center.ws.heartbeat;

import org.yx.hoststack.center.common.enums.RegisterNodeEnum;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class HeartbeatTask implements Delayed {
    private String serviceId;
    private long expirationTime;
    private RegisterNodeEnum type;
    private Consumer<Long> timeoutCallback;

    public HeartbeatTask(String serviceId, long timeoutThreshold, RegisterNodeEnum type, Consumer<Long> timeoutCallback) {
        this.serviceId = serviceId;
        this.type = type;
        this.expirationTime = System.currentTimeMillis() + (timeoutThreshold * 1000);
        System.out.println("HeartbeatTask : expirationTime:" + expirationTime);
        this.timeoutCallback = timeoutCallback;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long convert = unit.convert(expirationTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        return convert;
    }

    @Override
    public int compareTo(Delayed o) {
        if (this.expirationTime < ((HeartbeatTask) o).expirationTime) {
            return -1;
        } else if (this.expirationTime > ((HeartbeatTask) o).expirationTime) {
            return 1;
        }
        return 0;
    }

    public String getServiceId() {
        return serviceId;
    }

    public RegisterNodeEnum getType() {
        return type;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void executeTimeoutCallback() {
        try {
            if (timeoutCallback != null) {
                System.out.println("Executing timeout callback for " + serviceId);
                timeoutCallback.accept(expirationTime);
                System.out.println("Callback executed successfully for " + serviceId);
            } else {
                System.out.println("No timeout callback defined for " + serviceId);
            }
        } catch (Exception e) {
            System.out.println("Exception while executing callback for " + serviceId);
            e.printStackTrace();
        }
    }
}
