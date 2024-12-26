package org.yx.hoststack.common.utils;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class RLockUtils {
    private final RedissonClient redissonClient;

    public <T> T tryLock(Object lockId, String prefix, long waitTimeSec, long leaseTimeSec, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(String.format("host-stack:lock:%s:%s", prefix, lockId));
        try {
            // 获取锁，等待10秒，锁过期时间设置为5秒，防止死锁
            boolean locked = lock.tryLock(waitTimeSec, leaseTimeSec, TimeUnit.SECONDS);
            if (locked) {
                return supplier.get();
            } else {
                // 获取锁失败，加锁超时或锁等待超时
                return null;
            }
        } catch (InterruptedException e) {
            // 线程中断异常，抛出RuntimeException
            throw new RuntimeException("Thread interrupted while waiting for lock: " + lock.getName(), e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
