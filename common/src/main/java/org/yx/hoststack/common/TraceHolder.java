package org.yx.hoststack.common;

import lombok.experimental.UtilityClass;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.Map;
import java.util.function.Supplier;

@UtilityClass
public class TraceHolder {

    /**
     * 业务函数耗时测试函数，旨在统计函数执行周期时间消耗
     *
     * @param title    需要被监测的函数名或标记
     * @param supplier 目标函数
     * @param <T>
     * @return
     */
    public <T> T stopWatch(Map<String, String> title, Supplier<T> supplier) {
        long startMs = System.currentTimeMillis();
        try {
            return supplier.get();
        } finally {
            long endMs = System.currentTimeMillis();
            KvLogger kvLogger = KvLogger.instance(TraceHolder.class)
                    .p(LogFieldConstants.CostMs, endMs - startMs);
            if (title != null) {
                for (String key : title.keySet()) {
                    kvLogger.p(key, title.get(key));
                }
            }
            kvLogger.i();
        }
    }

    /**
     * 业务函数耗时测试函数，旨在统计函数执行周期时间消耗
     *
     * @param title    需要被监测的函数名或标记
     * @param function 目标函数
     * @return
     */
    public void stopWatch(Map<String, String> title, Runnable function) {
        long startMs = System.currentTimeMillis();
        try {
            function.run();
        } finally {
            long endMs = System.currentTimeMillis();
            KvLogger kvLogger = KvLogger.instance(TraceHolder.class)
                    .p(LogFieldConstants.CostMs, endMs - startMs);
            if (title != null) {
                for (String key : title.keySet()) {
                    kvLogger.p(key, title.get(key));
                }
            }
            kvLogger.i();
        }
    }
}


