package org.yx.hoststack.edge.common;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.google.common.collect.Maps;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class KvMappingChannelContextTempData {
    private static final Map<String, ChannelHandlerContext> TEMP_DATA = Maps.newConcurrentMap();
    private static final Map<String, Long> TEMP_DATA_ADD_TIME = Maps.newConcurrentMap();
    private final ScheduledExecutorService checkContextTimeoutData;

    public KvMappingChannelContextTempData() {
        checkContextTimeoutData = Executors.newSingleThreadScheduledExecutor(
                ThreadFactoryBuilder.create().setNamePrefix("checkChannelContextTempDataTimeoutData").build());
        checkContextTimeoutData.scheduleAtFixedRate(() -> {
            long curTs = System.currentTimeMillis();
            for (String key : TEMP_DATA_ADD_TIME.keySet()) {
                long addTs = TEMP_DATA_ADD_TIME.get(key);
                if ((addTs + 120 * 1000) <= curTs) {
                    KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, EdgeEvent.BUSINESS)
                            .p(LogFieldConstants.ACTION, EdgeEvent.Action.CLEAR_TEMP_CONTEXT_MAPPING)
                            .p("TempDataKey", key)
                            .i();
                    remove(key);
                }
            }

        }, 5, 120, TimeUnit.SECONDS);
    }

    public void put(String key, ChannelHandlerContext context) {
        TEMP_DATA.put(key, context);
        TEMP_DATA_ADD_TIME.put(key, System.currentTimeMillis());
    }

    public void remove(String key) {
        TEMP_DATA.remove(key);
        TEMP_DATA_ADD_TIME.remove(key);
    }

    public ChannelHandlerContext get(String key) {
        return TEMP_DATA.get(key);
    }

    public void destroy() {
        if (checkContextTimeoutData != null) {
            checkContextTimeoutData.shutdown();
        }
    }
}
