package org.yx.hoststack.center.queue.consumers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.jobs.JobManager;
import org.yx.hoststack.center.queue.MessageQueues;
import org.yx.hoststack.center.queue.message.JobReportMessage;
import org.yx.hoststack.common.QueueMessage;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class JobNotifyProcessConsumer implements Runnable {
    private final MessageQueues messageQueues;
    private final JobManager jobManager;

    @Override
    public void run() {
        while (true) {
            try {
                QueueMessage<JobReportMessage> queueMessage = messageQueues.getJobReportQueue().poll();
                if (queueMessage != null) {
                    jobManager.processJobResult(queueMessage.getMessage());
                } else {
                    TimeUnit.MILLISECONDS.sleep(10);
                }
            } catch (Exception e) {
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, CenterEvent.PROCESS_JOB_NOTIFY)
                        .p(LogFieldConstants.ERR_MSG, e.getMessage())
                        .e(e);
            }
        }
    }
}
