package org.yx.hoststack.edge.queue.consumers;

import cn.hutool.core.lang.UUID;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yx.hoststack.edge.client.EdgeClientConnector;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.queue.MessageQueues;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class JobNotifyToCenterConsumer implements Runnable {
    private final MessageQueues messageQueues;

    @Override
    public void run() {
        while (true) {
            try {
                List<AgentCommonMessage> agentCommonMessages = Lists.newArrayList();
                messageQueues.getJobNotifyToCenterQueue().drainTo(agentCommonMessages, 50);
                if (!agentCommonMessages.isEmpty()) {
                    EdgeClientConnector.getInstance().sendJobNotifyReport(agentCommonMessages, UUID.fastUUID().toString(), null,
                            () -> messageQueues.getJobNotifyToDiskQueue().addAll(agentCommonMessages));
                } else {
                    TimeUnit.MILLISECONDS.sleep(10);
                }
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.WORK_QUEUE_CONSUMER)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.CONSUMER_HOST_HB)
                        .p(LogFieldConstants.ERR_MSG, interruptedException.getMessage())
                        .e(interruptedException);
            } catch (Exception e) {
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.WORK_QUEUE_CONSUMER)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.CONSUMER_NOT_SEND_JOB_NOTIFY)
                        .p(LogFieldConstants.ERR_MSG, e.getMessage())
                        .e(e);
            }
        }
    }
}
