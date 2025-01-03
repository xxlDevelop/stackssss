package org.yx.hoststack.edge.transfer.manager;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.config.EdgeServerConfig;
import org.yx.hoststack.protocol.ws.ResendMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TransferNodeMgr {
    @Value("${idc.sessionTimeout:10}")
    private int idcSessionTimeout;
    private static final Map<String, TransferNode> transferNodeMap = Maps.newConcurrentMap();
    private ScheduledExecutorService reSendScheduler;
    private final EdgeServerConfig edgeServerConfig;

    public TransferNodeMgr(EdgeServerConfig edgeServerConfig) {
        this.edgeServerConfig = edgeServerConfig;
        startRetrySend();
    }

    private void startRetrySend() {
        reSendScheduler = Executors.newScheduledThreadPool(1,
                ThreadFactoryBuilder.create().setNamePrefix("transfer-node-resend").build());
        reSendScheduler.scheduleAtFixedRate(() -> {
            if (TransferReSendMap.getData().mappingCount() > 0) {
                for (Map.Entry<String, ResendMessage<CommonMessageWrapper.CommonMessage>> resendMessageEntry : TransferReSendMap.getData().entrySet()) {
                    ResendMessage<CommonMessageWrapper.CommonMessage> resendMessage = resendMessageEntry.getValue();
                    String resendMessageId = resendMessageEntry.getKey();
                    Channel resendChannel = resendMessage.getChannel();
                    try {
                        if (!resendChannel.isActive() || !resendChannel.isOpen() || !resendChannel.isWritable()) {
                            TransferReSendMap.remove(resendMessageId);
                            continue;
                        }
                        AtomicInteger retry = new AtomicInteger(resendMessage.getRetry());
                        if (retry.get() < edgeServerConfig.getRetryNumber()) {
//                            resendChannel.eventLoop().execute(() -> {
                            CommonMessageWrapper.CommonMessage reSendCommonMessage = resendMessage.getData();
                            ChannelFuture reSendChannelFuture = resendChannel.writeAndFlush(new TextWebSocketFrame(reSendCommonMessage.toString()));
                            resendMessage.setRetry(retry.incrementAndGet());
                            reSendChannelFuture.addListener(retryFuture -> {
                                if (retryFuture.isDone() && retryFuture.cause() != null) {
                                    KvLogger.instance(this)
                                            .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                                            .p(LogFieldConstants.ACTION, EdgeEvent.Action.ReSendMsgFailed)
                                            .p(LogFieldConstants.ERR_MSG, retryFuture.cause().getMessage())
                                            .p(LogFieldConstants.ReqData, reSendCommonMessage.toString())
                                            .p(HostStackConstants.CHANNEL_ID, resendChannel.id())
                                            .p(HostStackConstants.METH_ID, reSendCommonMessage.getHeader().getMethId())
                                            .p(HostStackConstants.TRACE_ID, reSendCommonMessage.getHeader().getTraceId())
                                            .p("ReSendId", resendMessage.getReSendId())
                                            .p("RetryTimes", retry.get())
                                            .e(retryFuture.cause());
                                } else if (retryFuture.isDone() && retryFuture.isSuccess()) {
                                    TransferReSendMap.remove(resendMessageId);
                                    KvLogger kvLogger = KvLogger.instance(this)
                                            .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                                            .p(LogFieldConstants.ACTION, EdgeEvent.Action.ReSendMsgSuccessful)
                                            .p(HostStackConstants.CHANNEL_ID, resendChannel.id())
                                            .p(HostStackConstants.METH_ID, reSendCommonMessage.getHeader().getMethId())
                                            .p(HostStackConstants.TRACE_ID, reSendCommonMessage.getHeader().getTraceId())
                                            .p("ReSendId", resendMessage.getReSendId())
                                            .p("RetryTimes", retry.get());
                                    kvLogger.i();
                                    kvLogger.p(LogFieldConstants.ReqData, reSendCommonMessage.toString()).d();
                                }
                            });
//                            });
                        } else {
                            TransferReSendMap.remove(resendMessageId);
                        }
                    } catch (Exception ex) {
                        KvLogger.instance(this)
                                .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                                .p(LogFieldConstants.ACTION, EdgeEvent.Action.ReSendMsgFailed)
                                .p(HostStackConstants.CHANNEL_ID, resendChannel.id())
                                .e(ex);
                    }
                }
            }
        }, 5, 60, TimeUnit.SECONDS);
    }

    public TransferNode addTransferNode(String nodeId, int hbInterval, ChannelHandlerContext context) {
        TransferNode node = new TransferNode(nodeId, context, idcSessionTimeout, hbInterval);
        addTransferNode(node);
        return node;
    }

    public void addTransferNode(TransferNode node) {
        node.registerTimeoutEvent(this::transferNodeTimeout);
        transferNodeMap.put(node.getNodeId(), node);
    }

    public void removeTransferNode(String nodeId) {
        transferNodeMap.remove(nodeId);
    }

    public TransferNode get(String nodeId) {
        return transferNodeMap.get(nodeId);
    }

    private void transferNodeTimeout(TransferNode transferNode) {
        transferNode.destroy();
        removeTransferNode(transferNode.getNodeId());
    }

    public void destroy() {
        if (reSendScheduler != null && !reSendScheduler.isShutdown()) {
            reSendScheduler.shutdown();
        }
        transferNodeMap.values().forEach(TransferNode::destroy);
    }
}
