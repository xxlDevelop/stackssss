package org.yx.hoststack.edge.queue.consumers;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.common.FileLock;
import org.yx.hoststack.edge.config.EdgeCommonConfig;
import org.yx.hoststack.edge.queue.MessageQueues;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
@RequiredArgsConstructor
public class NotSendJobNotifyConsumer implements Runnable {
    private final MessageQueues messageQueues;
    private final EdgeCommonConfig edgeCommonConfig;

    @Override
    public void run() {
        while (true) {
            try {
                List<AgentCommonMessage<?>> agentCommonMessages = Lists.newArrayList();
                messageQueues.getJobNotifyNotSendQueue().drainTo(agentCommonMessages, 50);
                if (!agentCommonMessages.isEmpty()) {
                    writeToFile(agentCommonMessages);
                } else {
                    TimeUnit.MILLISECONDS.sleep(10);
                }
            } catch (Exception e) {
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.WorkQueueConsumer)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.WorkQueueConsumer_ConsumerNotSendJobNotify)
                        .e(e);
            }
        }
    }

    private void writeToFile(List<AgentCommonMessage<?>> jobNotifyMessage) {
        try {
            Path filePath;
            String[] files = new File(edgeCommonConfig.getNotSendJobNotifySavePath()).list();
            if (files == null || files.length == 0) {
                filePath = Path.of(edgeCommonConfig.getNotSendJobNotifySavePath(), "jobNotify.0");
            } else {
                long maxSuffix = 0;
                for (String file : files) {
                    int suffix = Integer.parseInt(file.split("\\.")[1]);
                    if (suffix > maxSuffix) {
                        maxSuffix = suffix;
                    }
                }
                filePath = Path.of(edgeCommonConfig.getNotSendJobNotifySavePath(), "jobNotify." + maxSuffix);
                long fileSize = Files.size(filePath);
                if (DataSize.ofBytes(fileSize).compareTo(DataSize.ofMegabytes(20)) > 0) {
                    maxSuffix = maxSuffix + 1;
                    filePath = Path.of(edgeCommonConfig.getNotSendJobNotifySavePath(), "jobNotify." + maxSuffix);
                }
            }
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.JobNotifyToFile)
                    .p(LogFieldConstants.ACTION, "WriteJobNotifyFile")
                    .p("NotifyCount", jobNotifyMessage.size())
                    .p("FilePath", filePath.toString())
                    .i();
            ReentrantReadWriteLock readWriteLock = FileLock.getLock(HostStackConstants.LOCK_JOB_NOTIFY);
            readWriteLock.writeLock().lock();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toString(), true))) {
                for (AgentCommonMessage<?> message : jobNotifyMessage) {
                    writer.write(JSON.toJSONString(message));
                    writer.newLine();
                }
                writer.flush();
            } finally {
                readWriteLock.writeLock().unlock();
            }
        } catch (Exception ex) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.JobNotifyToFile)
                    .p(LogFieldConstants.ACTION, "WriteJobNotifyFile")
                    .p(LogFieldConstants.ERR_MSG, ex.getMessage())
                    .e(ex);
        }
    }
}
