package org.yx.hoststack.edge.client;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.common.syscode.EdgeSysCode;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.common.SendMsgCallback;
import org.yx.hoststack.edge.config.EdgeClientConfig;
import org.yx.hoststack.protocol.ws.ResendMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.SpringContextHolder;
import org.yx.lib.utils.util.StringUtil;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

abstract class EdgeClientConnectorBase {
    private static final ConcurrentHashMap<String, ResendMessage<CommonMessageWrapper.CommonMessage>> CLIENT_RE_SEND_MAP = new ConcurrentHashMap<>();
    protected ScheduledExecutorService edgeClientHbScheduler;
    protected final ScheduledExecutorService reSendMsgScheduler;
    protected final EdgeClientConfig edgeClientConfig;
    private Channel channel;

    public EdgeClientConnectorBase() {
        reSendMsgScheduler = Executors.newSingleThreadScheduledExecutor(
                ThreadFactoryBuilder.create().setNamePrefix("client-reSend-").build());
        edgeClientConfig = SpringContextHolder.getBean(EdgeClientConfig.class);
        startRetrySend();
    }

    protected void create(ChannelFuture channelFuture) {
        this.channel = channelFuture.channel();
    }

    public synchronized void disConnect() {
        if (edgeClientHbScheduler != null) {
            edgeClientHbScheduler.shutdownNow();
        }
        if (channel != null && channel.isOpen()) {
            channel.disconnect();
        }
    }

    public void destroy() {
        if (edgeClientHbScheduler != null) {
            edgeClientHbScheduler.shutdown();
        }
        if (reSendMsgScheduler != null) {
            reSendMsgScheduler.shutdown();
        }
        if (channel != null) {
            channel.close();
        }
    }

    public boolean isAlive() {
        return channel != null && channel.isOpen() && channel.isActive();
    }

    public void sendMsg(CommonMessageWrapper.CommonMessage msg,
                        SendMsgCallback successCallback, SendMsgCallback failCallback) {
        sendMsg0(msg, successCallback, failCallback);
    }

    protected void startHeartbeat(int hbInterval) {
        sendMsg(buildSendMessage(ProtoMethodId.Ping.getValue(), ByteString.EMPTY, UUID.fastUUID().toString()), null,
                this::disConnect);
        edgeClientHbScheduler = Executors.newSingleThreadScheduledExecutor(
                ThreadFactoryBuilder.create().setNamePrefix("client-hb-").build());
        edgeClientHbScheduler.scheduleAtFixedRate(() -> {
            try {
                sendMsg(buildSendMessage(ProtoMethodId.Ping.getValue(), ByteString.EMPTY, UUID.fastUUID().toString()), null,
                        this::disConnect);
            } catch (Exception ex) {
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_CLIENT)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.SEND_MSG_FAILED)
                        .p(HostStackConstants.CHANNEL_ID, channel.id())
                        .e(ex);
            }
        }, hbInterval, hbInterval, TimeUnit.SECONDS);
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
                        .setMsg(StringUtil.isBlank(msg) ? "" : msg))
                .build();
    }

    private void sendMsg0(CommonMessageWrapper.CommonMessage message, SendMsgCallback successCallback, SendMsgCallback failCallback) {
        KvLogger kvLogger = KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_CLIENT)
                .p(HostStackConstants.CHANNEL_ID, channel == null ? "" : channel.id())
                .p(HostStackConstants.TRACE_ID, message.getHeader().getTraceId())
                .p(HostStackConstants.METH_ID, message.getHeader().getMethId())
                .p(HostStackConstants.IDC_SID, message.getHeader().getIdcSid())
                .p(HostStackConstants.RELAY_SID, message.getHeader().getRelaySid())
                .p(HostStackConstants.REGION, EdgeContext.Region)
                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode);
        if (!isAlive()) {
            kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.SEND_MSG_FAILED)
                    .p(LogFieldConstants.ERR_MSG, channel == null ? "Channel is null" : "Channel is not active")
                    .w();
            Optional.ofNullable(failCallback).ifPresent(SendMsgCallback::callback);
            return;
        }

//        channel.eventLoop().execute(() -> {
        byte[] protobufMessage = message.toByteArray();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(protobufMessage);
        ChannelFuture channelFuture = channel.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
        channelFuture.addListener(future -> {
            if (future.isDone() && future.cause() != null) {
                kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.SEND_MSG_FAILED)
                        .p(LogFieldConstants.ERR_MSG, future.cause().getMessage())
                        .p(LogFieldConstants.Code, EdgeSysCode.SendMsgFailed.getValue())
                        .p(LogFieldConstants.ReqData, Base64.encode(message.toByteArray()))
                        .e(future.cause());
                putResendMessage(message);
            } else if (future.isDone() && future.isSuccess()) {
                kvLogger.p(LogFieldConstants.Code, 0)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.SEND_MSG_SUCCESSFUL);
                if (message.getHeader().getMethId() == ProtoMethodId.Ping.getValue() ||
                        message.getHeader().getMethId() == ProtoMethodId.Pong.getValue() || kvLogger.isDebug()) {
                    kvLogger.p(LogFieldConstants.ReqData, Base64.encode(message.toByteArray()))
                            .d();
                } else {
                    kvLogger.i();
                }
                Optional.ofNullable(successCallback).ifPresent(SendMsgCallback::callback);
            }
        });

//        });
    }

    private void putResendMessage(CommonMessageWrapper.CommonMessage msg) {
        String resendId = MessageFormat.format("{0}-{1}", channel.id(), msg.getHeader().getTraceId());
        ResendMessage<CommonMessageWrapper.CommonMessage> resendMessage = new ResendMessage<>();
        resendMessage.setReSendId(resendId);
        resendMessage.setData(msg);
        CLIENT_RE_SEND_MAP.putIfAbsent(resendId, resendMessage);
    }

    private void startRetrySend() {
        reSendMsgScheduler.scheduleAtFixedRate(() -> {
            if (CLIENT_RE_SEND_MAP.mappingCount() > 0) {
                CLIENT_RE_SEND_MAP.forEach((resendMessageId, resendMessage) -> {
                    CommonMessageWrapper.CommonMessage reSendProto = resendMessage.getData();
                    AtomicInteger retry = new AtomicInteger(resendMessage.getRetry());
                    KvLogger kvLogger = KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_CLIENT)
                            .p(HostStackConstants.CHANNEL_ID, channel.id())
                            .p(HostStackConstants.METH_ID, reSendProto.getHeader().getMethId())
                            .p(HostStackConstants.TRACE_ID, reSendProto.getHeader().getTraceId())
                            .p(HostStackConstants.IDC_SID, reSendProto.getHeader().getIdcSid())
                            .p(HostStackConstants.RELAY_SID, reSendProto.getHeader().getRelaySid())
                            .p(HostStackConstants.REGION, EdgeContext.Region)
                            .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                            .p("RetryTimes", retry.get())
                            .p("ReSendId", resendMessage.getReSendId());
                    if (!isAlive()) {
                        CLIENT_RE_SEND_MAP.remove(resendMessageId);
                        kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.RE_SEND_MSG_FAILED)
                                .p(LogFieldConstants.ERR_MSG, "Channel is not alive, drop this msg")
                                .p(LogFieldConstants.Code, EdgeSysCode.SendMsgFailed.getValue())
                                .w();
                        return;
                    }
                    try {
                        if (retry.get() >= edgeClientConfig.getRetryNumber()) {
                            CLIENT_RE_SEND_MAP.remove(resendMessageId);
                            kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.RE_SEND_MSG_FAILED_LIMIT)
                                    .p(LogFieldConstants.ERR_MSG, "retry send limit, drop this msg")
                                    .w();
                            return;
                        }
                        byte[] protobufMessage = reSendProto.toByteArray();
                        ByteBuf byteBuf = Unpooled.wrappedBuffer(protobufMessage);
                        ChannelFuture reSendChannelFuture = channel.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
                        resendMessage.setRetry(retry.incrementAndGet());
                        reSendChannelFuture.addListener(retryFuture -> {
                            if (retryFuture.isDone() && retryFuture.cause() != null) {
                                kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.RE_SEND_MSG_FAILED)
                                        .p(LogFieldConstants.ERR_MSG, retryFuture.cause().getMessage())
                                        .p(LogFieldConstants.Code, EdgeSysCode.SendMsgFailed.getValue())
                                        .p(LogFieldConstants.ReqData, Base64.encode(resendMessage.getData().toByteArray()))
                                        .p("RetryTimes", retry.get())
                                        .e(retryFuture.cause());
                            } else if (retryFuture.isDone() && retryFuture.isSuccess()) {
                                kvLogger.p(LogFieldConstants.Code, 0)
                                        .p("RetryTimes", retry.get());
                                kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.RE_SEND_MSG_SUCCESSFUL)
                                        .i();
                                if (kvLogger.isDebug()) {
                                    kvLogger.p(LogFieldConstants.ReqData, Base64.encode(resendMessage.getData().toByteArray()))
                                            .d();
                                }
                                CLIENT_RE_SEND_MAP.remove(resendMessageId);
                            }
                        });
                    } catch (Exception ex) {
                        KvLogger.instance(this)
                                .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_CLIENT)
                                .p(LogFieldConstants.ACTION, EdgeEvent.Action.RE_SEND_MSG_FAILED)
                                .p(HostStackConstants.CHANNEL_ID, channel.id())
                                .p(HostStackConstants.IDC_SID, reSendProto.getHeader().getIdcSid())
                                .p(HostStackConstants.RELAY_SID, reSendProto.getHeader().getRelaySid())
                                .p(HostStackConstants.REGION, EdgeContext.Region)
                                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                                .e(ex);
                    }
                });
            }
        }, 5, 10, TimeUnit.SECONDS);
    }
}
