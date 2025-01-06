package org.yx.hoststack.edge;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.yx.hoststack.edge.client.EdgeClient;
import org.yx.hoststack.edge.common.KvMappingChannelContextTempData;
import org.yx.hoststack.edge.forwarding.manager.ForwardingNodeMgr;
import org.yx.hoststack.edge.server.ws.EdgeServer;
import org.yx.lib.utils.logger.KvLogger;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

@Component
public class EdgeDispose implements DisposableBean {
    private final EdgeServer edgeServer;
    private final EdgeClient edgeClient;
    private final ForwardingNodeMgr forwardingNodeMgr;
    private final KvMappingChannelContextTempData kvMappingChannelContextTempData;
    private final Executor executor;

    public EdgeDispose(EdgeServer edgeServer,
                       EdgeClient edgeClient,
                       ForwardingNodeMgr forwardingNodeMgr,
                       KvMappingChannelContextTempData kvMappingChannelContextTempData,
                       @Qualifier("edgeExecutor") Executor executor) {
        this.edgeServer = edgeServer;
        this.edgeClient = edgeClient;
        this.forwardingNodeMgr = forwardingNodeMgr;
        this.kvMappingChannelContextTempData = kvMappingChannelContextTempData;
        this.executor = executor;
    }

    @Override
    public void destroy() {
        try {
            KvLogger.instance(this).p("Event", "EdgeServerDispose").i();
            edgeServer.destroy();
            edgeClient.destroy();
            forwardingNodeMgr.destroy();
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
