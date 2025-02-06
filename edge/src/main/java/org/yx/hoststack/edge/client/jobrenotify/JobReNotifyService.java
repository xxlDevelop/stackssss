package org.yx.hoststack.edge.client.jobrenotify;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.edge.apiservice.storagesvc.resp.ListBucketResp;
import org.yx.hoststack.edge.client.EdgeClientConnector;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.common.FileLock;
import org.yx.hoststack.edge.config.EdgeCommonConfig;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.ListUtil;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.StringPool;
import org.yx.lib.utils.util.StringUtil;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@RequiredArgsConstructor
public class JobReNotifyService {
    private final EdgeCommonConfig edgeCommonConfig;
    private final AtomicBoolean isReSending = new AtomicBoolean(false);

    public void writeToFile(List<AgentCommonMessage<?>> commonMessageList) {
        try {
            Path filePath;
            String[] files = new File(edgeCommonConfig.getNotSendJobNotifySavePath()).list();
            if (files == null || files.length == 0) {
                filePath = Path.of(edgeCommonConfig.getNotSendJobNotifySavePath(), "jobNotify." + String.format("%05d", 1));
            } else {
                long maxSuffix = 0;
                for (String file : files) {
                    int suffix = Integer.parseInt(file.split("\\.")[1]);
                    if (suffix > maxSuffix) {
                        maxSuffix = suffix;
                    }
                }
                filePath = Path.of(edgeCommonConfig.getNotSendJobNotifySavePath(), "jobNotify." + String.format("%05d", maxSuffix));
                long fileSize = Files.size(filePath);
                if (DataSize.ofBytes(fileSize).compareTo(DataSize.ofMegabytes(2)) > 0) {
                    maxSuffix = maxSuffix + 1;
                    filePath = Path.of(edgeCommonConfig.getNotSendJobNotifySavePath(), "jobNotify." + String.format("%05d", maxSuffix));
                }
            }
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.JOB_NOTIFY_TO_FILE)
                    .p(LogFieldConstants.ACTION, "WriteJobNotifyFile")
                    .p("NotifyCount", commonMessageList.size())
                    .p("FilePath", filePath.toString())
                    .i();
            ReentrantReadWriteLock readWriteLock = FileLock.getLock(HostStackConstants.LOCK_JOB_NOTIFY);
            readWriteLock.writeLock().lock();
            try (RandomAccessFile accessFile = new RandomAccessFile(filePath.toString(), "rw")) {
                try (FileChannel channel = accessFile.getChannel()) {
                    accessFile.seek(accessFile.length());
                    try {
                        ByteBuffer byteBuffer = toByteBuffer(commonMessageList);
                        channel.write(byteBuffer);
                    } catch (Exception ex) {
                        KvLogger.instance(this)
                                .p(LogFieldConstants.EVENT, EdgeEvent.JOB_NOTIFY_TO_FILE)
                                .p(LogFieldConstants.ACTION, "WriteJobNotifyFile")
                                .p("FilePath", filePath.toString())
                                .e(ex);
                    } finally {
                        readWriteLock.writeLock().unlock();
                    }
                }
            }
        } catch (Exception ex) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.JOB_NOTIFY_TO_FILE)
                    .p(LogFieldConstants.ACTION, "WriteJobNotifyFile")
                    .p(LogFieldConstants.ERR_MSG, ex.getMessage())
                    .e(ex);
        }
    }

    public void reSendJobNotify() {
        if (isReSending.get()) {
            return;
        }
        if (StringUtil.isBlank(EdgeContext.IdcId) && StringUtil.isBlank(EdgeContext.RelayId)) {
            return;
        }
        isReSending.set(true);
        try {
            String[] files = new File(edgeCommonConfig.getNotSendJobNotifySavePath()).list();
            if (files == null || files.length == 0) {
                return;
            }
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.JOB_NOTIFY_TO_FILE)
                    .p(LogFieldConstants.ACTION, "StartScanToSend")
                    .p("FileCount", files.length)
                    .i();
            if (!EdgeClientConnector.getInstance().isAlive()) {
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.JOB_NOTIFY_TO_FILE)
                        .p(LogFieldConstants.ACTION, "StartScanToSend")
                        .p(LogFieldConstants.ERR_MSG, "Client not alive, can`t send to upStream")
                        .p("FileCount", files.length)
                        .w();
                return;
            }
            for (String file : files) {
                ReentrantReadWriteLock readWriteLock = FileLock.getLock(HostStackConstants.LOCK_JOB_NOTIFY);
                readWriteLock.writeLock().lock();
                try {
                    Path targetPath = Path.of(edgeCommonConfig.getNotSendJobNotifySavePath(), file);
                    List<AgentCommonMessage<?>> contentList;
                    try (RandomAccessFile accessFile = new RandomAccessFile(targetPath.toString(), "rw")) {
                        try (FileChannel channel = accessFile.getChannel()) {
                            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
                            channel.read(buffer);
                            String content = new String(buffer.array(), StandardCharsets.UTF_8);
                            contentList = JSON.parseObject(StringPool.LEFT_SQ_BRACKET + content + StringPool.RIGHT_SQ_BRACKET,
                                    new TypeReference<List<AgentCommonMessage<?>>>() {
                                    });
                        }
                    }
                    List<AgentCommonMessage<?>> notSendMessageList = Lists.newArrayList();
                    if (!contentList.isEmpty()) {
                        List<List<AgentCommonMessage<?>>> batchList = ListUtil.batchList(contentList, 100);
                        for (List<AgentCommonMessage<?>> messageList : batchList) {
                            EdgeClientConnector.getInstance().sendJobNotifyReport(messageList, UUID.fastUUID().toString(), null,
                                    () -> notSendMessageList.addAll(messageList));
                        }
                        if (notSendMessageList.isEmpty()) {
                            FileUtils.delete(new File(targetPath.toString()));
                        } else {
                            try {
                                try (RandomAccessFile accessFile = new RandomAccessFile(targetPath.toString(), "rw")) {
                                    try (FileChannel channel = accessFile.getChannel()) {
                                        ByteBuffer byteBuffer = toByteBuffer(notSendMessageList);
                                        channel.write(byteBuffer);
                                    }
                                }
                            } catch (Exception ex) {
                                KvLogger.instance(this).p(LogFieldConstants.EVENT, EdgeEvent.JOB_NOTIFY_TO_FILE)
                                        .p(LogFieldConstants.ACTION, "StartScanToSend")
                                        .p(LogFieldConstants.ERR_MSG, ex.getMessage())
                                        .p("FilePath", Path.of(edgeCommonConfig.getNotSendJobNotifySavePath(), file).toString())
                                        .e(ex);
                            }
                        }
                    }
                } catch (Exception ex) {
                    KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, EdgeEvent.JOB_NOTIFY_TO_FILE)
                            .p(LogFieldConstants.ACTION, "StartScanToSend")
                            .p(LogFieldConstants.ERR_MSG, ex.getMessage())
                            .p("FilePath", Path.of(edgeCommonConfig.getNotSendJobNotifySavePath(), file).toString())
                            .e(ex);
                } finally {
                    readWriteLock.writeLock().unlock();
                }
            }
        } finally {
            isReSending.set(false);
        }
    }

    private ByteBuffer toByteBuffer(List<AgentCommonMessage<?>> messageList) {
        List<byte[]> bytesList = new ArrayList<>();
        int size = 0;
        for (AgentCommonMessage<?> message : messageList) {
            byte[] bytesContent = (JSON.toJSONString(message) + StringPool.NEWLINE).getBytes(StandardCharsets.UTF_8);
            bytesList.add(bytesContent);
            size += bytesContent.length;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        for (byte[] bytes : bytesList) {
            byteBuffer.put(bytes);
        }
        return byteBuffer.flip();
    }
}
