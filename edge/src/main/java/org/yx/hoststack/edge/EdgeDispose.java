package org.yx.hoststack.edge;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.yx.hoststack.edge.client.EdgeClient;
import org.yx.hoststack.edge.common.KvMappingChannelContextTempData;
import org.yx.hoststack.edge.server.ws.EdgeServer;
import org.yx.hoststack.edge.transfer.manager.TransferNodeMgr;
import org.yx.lib.utils.logger.KvLogger;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

@Component
@RequiredArgsConstructor
public class EdgeDispose implements DisposableBean {
    private final EdgeServer edgeServer;
    private final EdgeClient edgeClient;
    private final TransferNodeMgr transferNodeMgr;
    private final KvMappingChannelContextTempData kvMappingChannelContextTempData;
    private final @Qualifier("edgeExecutor") Executor executor;

    @Override
    public void destroy() {
        try {
            KvLogger.instance(this).p("Event", "EdgeServerDispose").i();
            edgeServer.destroy();
            edgeClient.destroy();
            transferNodeMgr.destroy();
            kvMappingChannelContextTempData.destroy();
            if (executor instanceof ExecutorService executorService) {
                executorService.shutdownNow();
            }
            if (executor instanceof ThreadPoolTaskExecutor threadPoolTaskExecutor) {
                threadPoolTaskExecutor.shutdown();
            }
        } catch (Exception ex) {
            KvLogger.instance(this).p("Event", "EdgeServerDispose").e(ex);
        }
    }
}
