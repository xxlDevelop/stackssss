package org.yx.hoststack.center.ws.task;

import org.yx.hoststack.center.common.enums.RegisterNodeEnum;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class HeartbeatTask implements Delayed {
    private String serviceId;
    private long expirationTime;
    private RegisterNodeEnum type;
    private Runnable timeoutCallback;

    public HeartbeatTask(String serviceId, long timeoutThreshold, RegisterNodeEnum type, Runnable timeoutCallback) {
        this.serviceId = serviceId;
        this.type = type;
        this.expirationTime = System.currentTimeMillis() + (timeoutThreshold * 1000);
        this.timeoutCallback = timeoutCallback;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long convert = unit.convert(expirationTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        System.out.println(convert);
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
        if (timeoutCallback != null) {
            System.out.println("Executing timeout callback for " + serviceId);
            timeoutCallback.run();
        } else {
            System.out.println("No timeout callback set for " + serviceId);
        }
    }
}
