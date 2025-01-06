package org.yx.hoststack.edge.forwarding.manager;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.common.syscode.EdgeSysCode;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.config.EdgeServerConfig;
import org.yx.hoststack.protocol.ws.ResendMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ForwardingNodeMgr {
    @Value("${idc.sessionTimeout:10}")
    private int idcSessionTimeout;
    private static final Map<String, ForwardingNode> FORWARDING_NODE_MAP = Maps.newConcurrentMap();
    private ScheduledExecutorService reSendScheduler;
    private final EdgeServerConfig edgeServerConfig;

    public ForwardingNodeMgr(EdgeServerConfig edgeServerConfig) {
        this.edgeServerConfig = edgeServerConfig;
        startRetrySend();
    }

    private void startRetrySend() {
        reSendScheduler = Executors.newScheduledThreadPool(1,
                ThreadFactoryBuilder.create().setNamePrefix("transfer-node-resend").build());
        reSendScheduler.scheduleAtFixedRate(() -> {
            if (ForwardingReSendMap.getData().mappingCount() > 0) {
                for (Map.Entry<String, ResendMessage<CommonMessageWrapper.CommonMessage>> resendMessageEntry : ForwardingReSendMap.getData().entrySet()) {
                    ResendMessage<CommonMessageWrapper.CommonMessage> resendMessage = resendMessageEntry.getValue();
                    String resendMessageId = resendMessageEntry.getKey();
                    Channel resendChannel = resendMessage.getChannel();
                    CommonMessageWrapper.CommonMessage reSendCommonMessage = resendMessage.getData();
                    try {
                        if (!resendChannel.isActive() || !resendChannel.isOpen() || !resendChannel.isWritable()) {
                            ForwardingReSendMap.remove(resendMessageId);
                            continue;
                        }
                        AtomicInteger retry = new AtomicInteger(resendMessage.getRetry());
                        KvLogger kvLogger = KvLogger.instance(this)
                                .p(LogFieldConstants.EVENT, EdgeEvent.FORWARDING_PROTOCOL)
                                .p(HostStackConstants.CHANNEL_ID, resendChannel.id())
                                .p(HostStackConstants.METH_ID, reSendCommonMessage.getHeader().getMethId())
                                .p(HostStackConstants.TRACE_ID, reSendCommonMessage.getHeader().getTraceId())
                                .p(HostStackConstants.IDC_SID, reSendCommonMessage.getHeader().getIdcSid())
                                .p(HostStackConstants.RELAY_SID, reSendCommonMessage.getHeader().getRelaySid())
                                .p(HostStackConstants.REGION, EdgeContext.Region)
                                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                                .p("RetryTimes", retry.get());
                        if (retry.get() < edgeServerConfig.getRetryNumber()) {
                            ChannelFuture reSendChannelFuture = resendChannel.writeAndFlush(new TextWebSocketFrame(reSendCommonMessage.toString()));
                            resendMessage.setRetry(retry.incrementAndGet());
                            reSendChannelFuture.addListener(retryFuture -> {
                                if (retryFuture.isDone() && retryFuture.cause() != null) {
                                    kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.RE_SEND_MSG_FAILED)
                                            .p(LogFieldConstants.ERR_MSG, retryFuture.cause().getMessage())
                                            .p(LogFieldConstants.Code, EdgeSysCode.SendMsgFailed.getValue())
                                            .p(LogFieldConstants.ReqData, Base64.encode(reSendCommonMessage.toByteArray()))
                                            .e(retryFuture.cause());
                                } else if (retryFuture.isDone() && retryFuture.isSuccess()) {
                                    ForwardingReSendMap.remove(resendMessageId);
                                    kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.RE_SEND_MSG_SUCCESSFUL)
                                            .p(LogFieldConstants.Code, 0)
                                            .i();
                                    if (kvLogger.isDebug()) {
                                        kvLogger.p(LogFieldConstants.ReqData, Base64.encode(reSendCommonMessage.toByteArray())).d();
                                    }
                                }
                            });
                        } else {
                            kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.RE_SEND_MSG_FAILED_LIMIT)
                                    .w();
                            ForwardingReSendMap.remove(resendMessageId);
                        }
                    } catch (Exception ex) {
                        KvLogger.instance(this)
                                .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_SERVER)
                                .p(LogFieldConstants.ACTION, EdgeEvent.Action.RE_SEND_MSG_FAILED)
                                .p(HostStackConstants.CHANNEL_ID, resendChannel.id())
                                .p(HostStackConstants.METH_ID, reSendCommonMessage.getHeader().getMethId())
                                .p(HostStackConstants.TRACE_ID, reSendCommonMessage.getHeader().getTraceId())
                                .p(HostStackConstants.IDC_SID, reSendCommonMessage.getHeader().getIdcSid())
                                .p(HostStackConstants.RELAY_SID, reSendCommonMessage.getHeader().getRelaySid())
                                .p(HostStackConstants.REGION, EdgeContext.Region)
                                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                                .e(ex);
                    }
                }
            }
        }, 5, 60, TimeUnit.SECONDS);
    }

    public ForwardingNode addForwardingNode(String nodeId, int hbInterval, ChannelHandlerContext context) {
        ForwardingNode node = new ForwardingNode(nodeId, context, idcSessionTimeout, hbInterval);
        addForwardingNode(node);
        return node;
    }

    public void addForwardingNode(ForwardingNode node) {
        node.registerTimeoutEvent(this::forwardingNodeTimeout);
        FORWARDING_NODE_MAP.put(node.getNodeId(), node);
    }

    public void removeForwardingNode(String nodeId) {
        FORWARDING_NODE_MAP.remove(nodeId);
    }

    public Optional<ForwardingNode> get(String nodeId) {
        return Optional.ofNullable(FORWARDING_NODE_MAP.get(nodeId));
    }

    private void forwardingNodeTimeout(ForwardingNode forwardingNode) {
        forwardingNode.destroy();
        removeForwardingNode(forwardingNode.getNodeId());
    }

    public void destroy() {
        if (reSendScheduler != null) {
            reSendScheduler.shutdown();
        }
        FORWARDING_NODE_MAP.values().forEach(ForwardingNode::destroy);
    }
}
