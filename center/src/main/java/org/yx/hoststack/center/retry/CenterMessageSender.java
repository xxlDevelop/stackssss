package org.yx.hoststack.center.retry;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yx.hoststack.center.common.config.channel.ChannelSendConfig;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.req.channel.SendChannelReq;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class CenterMessageSender {

    private final ChannelSendConfig channelSendConfig;
    public CenterMessageSender(ChannelSendConfig channelSendConfig) {
        this.channelSendConfig = channelSendConfig;
    }

    private final ScheduledExecutorService reSendMsgScheduler = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNamePrefix("center-reSend-").build());

    // Use ConcurrentHashMap to store the resending message. The key is the message ID, and the value is the encapsulated object for resending the message
    public final ConcurrentHashMap<String, ReSendMessage> reSendMap = new ConcurrentHashMap<>();

    @Data
    @AllArgsConstructor
    public static class ReSendMessage {
        private final Channel channel;
        private final SendChannelReq request;
        private int retry;
        private final String messageId;
    }

    @PostConstruct
    public void init() {
        startRetrySend();
    }

    private void startRetrySend() {
        reSendMsgScheduler.scheduleAtFixedRate(() -> {
            if (!reSendMap.isEmpty()) {
                reSendMap.forEach((messageId, reSendMessage) -> {
                    Channel channel = reSendMessage.getChannel();
                    SendChannelReq request = reSendMessage.getRequest();
                    try {
                        KvLogger kvLogger = KvLogger.instance(this)
                                .p(LogFieldConstants.EVENT, CenterEvent.SEND_MSG_TO_CHANNEL_EVENT)
                                .p("MessageId", messageId)
                                .p("RetryTimes", reSendMessage.getRetry());

                        //Checking channel Status
                        if (!channel.isActive() || !channel.isOpen() || !channel.isWritable()) {
                            reSendMap.remove(messageId);
                            kvLogger.p(LogFieldConstants.ACTION, "RE_SEND_MSG_FAILED")
                                    .p(LogFieldConstants.ERR_MSG, "Channel is not alive")
                                    .w();
                            return;
                        }

                        // Check retry
                        if (reSendMessage.getRetry() >= channelSendConfig.getRetryNumber()) {
                            reSendMap.remove(messageId);
                            kvLogger.p(LogFieldConstants.ACTION, "RE_SEND_MSG_FAILED_LIMIT")
                                    .w();
                            return;
                        }

                        //EXECUTE RETRY
                        ChannelFuture reSendChannelFuture = channel.writeAndFlush(
                                new BinaryWebSocketFrame(Unpooled.wrappedBuffer(request.getMsg()))
                        );

                        reSendMessage.setRetry(reSendMessage.getRetry() + 1);

                        reSendChannelFuture.addListener(retryFuture -> {
                            if (retryFuture.isDone() && retryFuture.cause() != null) {
                                kvLogger.p(LogFieldConstants.ACTION, "RE_SEND_MSG_FAILED")
                                        .p(LogFieldConstants.ERR_MSG, retryFuture.cause().getMessage())
                                        .p(LogFieldConstants.ReqData, request.toString())
                                        .e(retryFuture.cause());
                            } else if (retryFuture.isDone() && retryFuture.isSuccess()) {
                                reSendMap.remove(messageId);
                                kvLogger.p(LogFieldConstants.ACTION, "RE_SEND_MSG_SUCCESSFUL")
                                        .i();
                                if (kvLogger.isDebug()) {
                                    kvLogger.p(LogFieldConstants.ReqData, request.toString())
                                            .d();
                                }
                            }
                        });
                    } catch (Exception ex) {
                        KvLogger.instance(this)
                                .p(LogFieldConstants.EVENT, CenterEvent.SEND_MSG_TO_CHANNEL_EVENT)
                                .p(LogFieldConstants.ACTION, "RE_SEND_MSG_FAILED")
                                .p("MessageId", messageId)
                                .e(ex);
                    }
                });
            }
        }, channelSendConfig.getRetryInterval(), channelSendConfig.getRetryInterval(), TimeUnit.SECONDS);
    }

    public String generateMessageId() {
        // Implement message ID generation logic to ensure uniqueness
        return UUID.randomUUID().toString();
    }

    @PreDestroy
    public void destroy() {
        reSendMsgScheduler.shutdown();
        reSendMap.clear();
    }
}