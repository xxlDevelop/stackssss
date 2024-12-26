package org.yx.hoststack.edge.common;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileLock {

    private static final Map<String, ReentrantReadWriteLock> LOCK_MAP = Maps.newConcurrentMap();
    private static final ReentrantReadWriteLock RWL = new ReentrantReadWriteLock();

    private FileLock() {
    }

    public static ReentrantReadWriteLock getLock(String lockName) {
        if (LOCK_MAP.containsKey(lockName)) {
            return LOCK_MAP.get(lockName);
        } else {
            ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
            LOCK_MAP.put(lockName, readWriteLock);
            return readWriteLock;
        }
    }

    public static void removeLock(String lockName) {
        LOCK_MAP.remove(lockName);
    }
}
