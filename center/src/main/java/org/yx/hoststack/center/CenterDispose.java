package org.yx.hoststack.center;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.yx.hoststack.center.retry.CenterMessageSender;
import org.yx.hoststack.center.ws.CenterServer;
import org.yx.lib.utils.logger.KvLogger;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

@Component
public class CenterDispose implements DisposableBean {
    private final CenterServer centerServer;
    private final CenterMessageSender centerMessageSender;
    private final Executor executor;

    public CenterDispose(CenterServer centerServer,
                         CenterMessageSender centerMessageSender,
                         @Qualifier("centerExecutor") Executor executor) {
        this.centerServer = centerServer;
        this.centerMessageSender = centerMessageSender;
        this.executor = executor;
    }

    @Override
    public void destroy() {
        try {
            KvLogger.instance(this).p("Event", "CenterServerDispose").i();
            centerServer.destroy();
            centerMessageSender.destroy();
            if (executor instanceof ExecutorService executorService) {
                executorService.shutdownNow();
            }
            if (executor instanceof ThreadPoolTaskExecutor threadPoolTaskExecutor) {
                threadPoolTaskExecutor.shutdown();
            }
        } catch (Exception ex) {
            KvLogger.instance(this).p("Event", "CenterServerDispose").e(ex);
        }
    }
}
