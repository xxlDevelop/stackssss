package org.yx.hoststack.center.queue;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.yx.hoststack.center.queue.message.JobReportMessage;
import org.yx.hoststack.common.QueueMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@Getter
public class MessageQueues {
    private final BlockingQueue<QueueMessage<JobReportMessage>> jobReportQueue = new LinkedBlockingQueue<>();
}
