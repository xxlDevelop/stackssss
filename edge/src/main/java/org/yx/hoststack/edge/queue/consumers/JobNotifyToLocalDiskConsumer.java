package org.yx.hoststack.edge.queue.consumers;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yx.hoststack.edge.client.jobrenotify.JobReNotifyService;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.queue.MessageQueues;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class JobNotifyToLocalDiskConsumer implements Runnable {
    private final MessageQueues messageQueues;
    private final JobReNotifyService jobReNotifyService;

    @Override
    public void run() {
        while (true) {
            try {
                List<AgentCommonMessage<?>> agentCommonMessages = Lists.newArrayList();
                messageQueues.getJobNotifyToDiskQueue().drainTo(agentCommonMessages, 50);
                if (!agentCommonMessages.isEmpty()) {
                    jobReNotifyService.writeToFile(agentCommonMessages);
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
