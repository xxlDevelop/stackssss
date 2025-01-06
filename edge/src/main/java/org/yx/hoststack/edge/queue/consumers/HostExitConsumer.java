package org.yx.hoststack.edge.queue.consumers;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.queue.MessageQueues;
import org.yx.hoststack.edge.server.ws.session.Session;
import org.yx.hoststack.edge.server.ws.session.SessionManager;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class HostExitConsumer implements Runnable {
    private final MessageQueues messageQueues;
    private final SessionManager sessionManager;

    @Override
    public void run() {
        while (true) {
            try {
                List<Session> batchHostExit = Lists.newArrayList();
                messageQueues.getHostExitQueue().drainTo(batchHostExit, 50);
                if (!batchHostExit.isEmpty()) {
                    for (Session hostSession : batchHostExit) {
                        sessionManager.closeSession(hostSession);
                    }
                } else {
                    TimeUnit.MILLISECONDS.sleep(10);
                }
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.WORK_QUEUE_CONSUMER)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.CONSUMER_HOST_EXIT)
                        .p(LogFieldConstants.ERR_MSG, interruptedException.getMessage())
                        .e(interruptedException);
            } catch (Exception e) {
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.WORK_QUEUE_CONSUMER)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.CONSUMER_HOST_EXIT)
                        .p(LogFieldConstants.ERR_MSG, e.getMessage())
                        .e(e);
            }
        }
    }
}
