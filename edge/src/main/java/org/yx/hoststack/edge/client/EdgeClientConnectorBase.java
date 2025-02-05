package org.yx.hoststack.edge.client;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.common.SendMsgCallback;
import org.yx.hoststack.edge.config.EdgeClientConfig;
import org.yx.hoststack.edge.config.EdgeCommonConfig;
import org.yx.hoststack.protocol.ws.ResendMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.SpringContextHolder;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

abstract class EdgeClientConnectorBase {
    private static final ConcurrentHashMap<String, ResendMessage<CommonMessageWrapper.CommonMessage>> ClientReSendMap = new ConcurrentHashMap<>();
    protected final ScheduledExecutorService edgeClientHbScheduler;
    protected final EdgeClientConfig edgeClientConfig;
    protected final EdgeCommonConfig edgeCommonConfig;
    private ChannelFuture channelFuture;
    private Channel channel;

    public EdgeClientConnectorBase() {
        edgeClientHbScheduler = Executors.newScheduledThreadPool(1,
                ThreadFactoryBuilder.create().setNamePrefix("edge-client-hb").build());
        edgeClientConfig = SpringContextHolder.getBean(EdgeClientConfig.class);
        edgeCommonConfig = SpringContextHolder.getBean(EdgeCommonConfig.class);
    }

    public void create(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
        this.channel = channelFuture.channel();
        startRetrySend();
    }

    public void close() {
        if (edgeClientHbScheduler != null && !edgeClientHbScheduler.isShutdown()) {
            edgeClientHbScheduler.shutdown();
        }
        if (channel.isOpen() || channel.isActive()) {
            channel.close();
        }
    }

    public boolean isAlive() {
        return channel == null || !channel.isOpen() || !channel.isActive() || !channel.isWritable();
    }

    public void sendMsg(CommonMessageWrapper.CommonMessage msg,
                        SendMsgCallback successCallback, SendMsgCallback failCallback) {
        sendMsg0(msg, successCallback, failCallback);
    }

    protected void startHeartbeat(int hbInterval) {
        edgeClientHbScheduler.scheduleAtFixedRate(() -> {
            try {
                sendMsg(buildSendMessage(ProtoMethodId.Ping.getValue(), ByteString.EMPTY, UUID.fastUUID().toString()), null, null);
            } catch (Exception ex) {
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.SendMsgFailed)
                        .p(HostStackConstants.CHANNEL_ID, channel.id())
                        .e(ex);
            }
        }, 5, hbInterval, TimeUnit.SECONDS);
    }

    public CommonMessageWrapper.CommonMessage buildSendMessage(int methodId, ByteString payload, String traceId) {
        return CommonMessageWrapper.CommonMessage.newBuilder()
                .setHeader(CommonMessageWrapper.CommonMessage.newBuilder().getHeaderBuilder()
                        .setLinkSide(CommonMessageWrapper.ENUM_LINK_SIDE.EDGE_TO_CENTER_VALUE)
                        .setProtocolVer(CommonMessageWrapper.ENUM_PROTOCOL_VER.PROTOCOL_V1_VALUE)
                        .setZone(EdgeContext.Zone)
                        .setRegion(EdgeContext.Region)
                        .setIdcSid(EdgeContext.IdcServiceId)
                        .setRelaySid(EdgeContext.RelayServiceId)
                        .setMethId(methodId)
                        .setTraceId(traceId))
                .setBody(CommonMessageWrapper.CommonMessage.newBuilder().getBodyBuilder()
                        .setPayload(payload))
                .build();
    }

    public CommonMessageWrapper.CommonMessage buildResultMessage(int methodId, int code, String msg, ByteString payload, String traceId) {
        return CommonMessageWrapper.CommonMessage.newBuilder()
                .setHeader(CommonMessageWrapper.CommonMessage.newBuilder().getHeaderBuilder()
                        .setLinkSide(CommonMessageWrapper.ENUM_LINK_SIDE.EDGE_TO_CENTER_VALUE)
                        .setProtocolVer(CommonMessageWrapper.ENUM_PROTOCOL_VER.PROTOCOL_V1_VALUE)
                        .setZone(EdgeContext.Zone)
                        .setRegion(EdgeContext.Region)
                        .setIdcSid(EdgeContext.IdcServiceId)
                        .setRelaySid(EdgeContext.RelayServiceId)
                        .setMethId(methodId)
                        .setTraceId(traceId))
                .setBody(CommonMessageWrapper.CommonMessage.newBuilder().getBodyBuilder()
                        .setPayload(payload)
                        .setCode(code)
                        .setMsg(msg))
                .build();
    }

    private void sendMsg0(CommonMessageWrapper.CommonMessage message, SendMsgCallback successCallback, SendMsgCallback failCallback) {
        KvLogger kvLogger = KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                .p(HostStackConstants.CHANNEL_ID, channel.id())
                .p(HostStackConstants.TRACE_ID, message.getHeader().getTraceId())
                .p(HostStackConstants.METH_ID, message.getHeader().getMethId())
                .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                .p(HostStackConstants.RELAY_SID, EdgeContext.RelayId)
                .p(HostStackConstants.REGION, EdgeContext.Region)
                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode);
        if (isAlive()) {
            kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.SendMsgFailed)
                    .p(LogFieldConstants.ERR_MSG, channel == null ? "Channel is null" : "Channel is not active")
                    .e();
            Optional.ofNullable(failCallback).ifPresent(SendMsgCallback::callback);
            return;
        }
        channel.eventLoop().execute(() -> {
            byte[] protobufMessage = message.toByteArray();
            ByteBuf byteBuf = Unpooled.wrappedBuffer(protobufMessage);
            ChannelFuture channelFuture = channel.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
            channelFuture.addListener(future -> {
                if (future.isDone() && future.cause() != null) {
                    kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.SendMsgFailed)
                            .p(LogFieldConstants.ERR_MSG, future.cause().getMessage())
                            .p(LogFieldConstants.ReqData, message.toString())
                            .e(future.cause());
                    putResendMessage(message);
                } else if (future.isDone() && future.isSuccess()) {
                    kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.SendMsgSuccessful)
                            .i();
                    kvLogger.p(LogFieldConstants.ReqData, message.toString())
                            .d();
                    Optional.ofNullable(successCallback).ifPresent(SendMsgCallback::callback);
                }
            });
        });
    }

    private void putResendMessage(CommonMessageWrapper.CommonMessage msg) {
        String resendId = MessageFormat.format("{0}-{1}", channel.id(), msg.getHeader().getTraceId());
        ResendMessage<CommonMessageWrapper.CommonMessage> resendMessage = new ResendMessage<>();
        resendMessage.setReSendId(resendId);
        resendMessage.setData(msg);
        ClientReSendMap.putIfAbsent(resendId, resendMessage);
    }

    private void startRetrySend() {
        channelFuture.addListener((ChannelFutureListener) channelFuture -> {
            Channel channel = channelFuture.channel();
            channel.eventLoop().scheduleAtFixedRate(() -> {
                if (ClientReSendMap.mappingCount() > 0) {
                    ClientReSendMap.forEach((resendMessageId, resendMessage) -> {
                        CommonMessageWrapper.CommonMessage reSendProto = resendMessage.getData();
                        AtomicInteger retry = new AtomicInteger(resendMessage.getRetry());
                        KvLogger kvLogger = KvLogger.instance(this)
                                .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                                .p(HostStackConstants.CHANNEL_ID, channel.id())
                                .p(HostStackConstants.METH_ID, reSendProto.getHeader().getMethId())
                                .p(HostStackConstants.TRACE_ID, reSendProto.getHeader().getTraceId())
                                .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                                .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                                .p(HostStackConstants.REGION, EdgeContext.Region)
                                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                                .p("RetryTimes", retry.get())
                                .p("ReSendId", resendMessage.getReSendId());
                        if (isAlive()) {
                            ClientReSendMap.remove(resendMessageId);
                            kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.ReSendMsgFailed)
                                    .p(LogFieldConstants.ERR_MSG, "Channel is not alive")
                                    .w();
                            return;
                        }
                        try {
                            if (retry.get() >= edgeClientConfig.getRetryNumber()) {
                                ClientReSendMap.remove(resendMessageId);
                                kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.ReSendMsgFailedLimit)
                                        .w();
                                return;
                            }
                            try (EventLoop channelEventloop = channel.eventLoop()) {
                                channelEventloop.execute(() -> {
                                    byte[] protobufMessage = reSendProto.toByteArray();
                                    ByteBuf byteBuf = Unpooled.wrappedBuffer(protobufMessage);
                                    ChannelFuture reSendChannelFuture = channel.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
                                    resendMessage.setRetry(retry.incrementAndGet());
                                    reSendChannelFuture.addListener(retryFuture -> {
                                        if (retryFuture.isDone() && retryFuture.cause() != null) {
                                            kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.ReSendMsgFailed)
                                                    .p(LogFieldConstants.ERR_MSG, retryFuture.cause().getMessage())
                                                    .p(LogFieldConstants.ReqData, reSendProto.toString())
                                                    .e(retryFuture.cause());
                                        } else if (retryFuture.isDone() && retryFuture.isSuccess()) {
                                            kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.ReSendMsgSuccessful)
                                                    .i();
                                            kvLogger.p(LogFieldConstants.ReqData, resendMessage.getData())
                                                    .d();
                                            ClientReSendMap.remove(resendMessageId);
                                        }
                                    });
                                });
                            }
                        } catch (Exception ex) {
                            KvLogger.instance(this)
                                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsClient)
                                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.ReSendMsgFailed)
                                    .p(HostStackConstants.CHANNEL_ID, channel.id())
                                    .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                                    .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                                    .p(HostStackConstants.REGION, EdgeContext.Region)
                                    .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                                    .e(ex);
                        }
                    });
                }
            }, 5, 10, TimeUnit.SECONDS);
        });
    }
}
