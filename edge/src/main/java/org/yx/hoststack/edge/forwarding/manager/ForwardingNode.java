package org.yx.hoststack.edge.forwarding.manager;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.thread.ThreadFactoryBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.Getter;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.common.syscode.EdgeSysCode;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

@Getter
public class ForwardingNode {
    private final String nodeId;
    private final ChannelHandlerContext context;
    private final long createTimeAt;
    private final AtomicLong lastUpdateHbAt;
    private final int sessionTimeout;
    private final int hbInterval;
    private Consumer<ForwardingNode> timeoutConsumer;
    private final ScheduledExecutorService checkHbScheduler;

    public ForwardingNode(String nodeId, ChannelHandlerContext context, int sessionTimeout, int hbInterval) {
        this.nodeId = nodeId;
        this.context = context;
        this.createTimeAt = System.currentTimeMillis();
        this.lastUpdateHbAt = new AtomicLong(createTimeAt);
        this.sessionTimeout = sessionTimeout;
        this.hbInterval = hbInterval;
        checkHbScheduler = Executors.newScheduledThreadPool(1,
                ThreadFactoryBuilder.create().setNamePrefix("transfer-node-hb-check" + context.channel().id()).build());
        checkHbScheduler.scheduleAtFixedRate(() -> {
            long diff = System.currentTimeMillis() - this.lastUpdateHbAt.get();
            if (diff > sessionTimeout * 1000L && timeoutConsumer != null) {
                timeoutConsumer.accept(this);
            }
        }, 5, hbInterval, TimeUnit.SECONDS);
    }

    public void registerTimeoutEvent(Consumer<ForwardingNode> timeoutConsumer) {
        this.timeoutConsumer = timeoutConsumer;
    }

    public void hb() {
        this.lastUpdateHbAt.set(System.currentTimeMillis());
    }

    public void sendMsg(CommonMessageWrapper.CommonMessage message) {
        KvLogger kvLogger = KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.FORWARDING_PROTOCOL)
                .p(HostStackConstants.CHANNEL_ID, context.channel().id())
                .p(HostStackConstants.TRACE_ID, message.getHeader().getTraceId())
                .p(HostStackConstants.METH_ID, message.getHeader().getMethId())
                .p(HostStackConstants.IDC_SID, message.getHeader().getIdcSid())
                .p(HostStackConstants.RELAY_SID, message.getHeader().getRelaySid())
                .p(HostStackConstants.REGION, EdgeContext.Region)
                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode);
        byte[] protobufMessage = message.toByteArray();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(protobufMessage);
        ChannelFuture channelFuture = context.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
        channelFuture.addListener(future -> {
            if (future.isDone() && future.cause() != null) {
                kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.FORWARDING_TO_IDC_FAIL)
                        .p(LogFieldConstants.ERR_MSG, future.cause().getMessage())
                        .p(LogFieldConstants.ReqData, Base64.encode(message.toByteArray()))
                        .p(LogFieldConstants.Code, EdgeSysCode.SendMsgFailed.getValue())
                        .e(future.cause());
                ForwardingReSendMap.putResendMessage(context.channel(), message);
            } else if (future.isDone() && future.isSuccess()) {
                kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.FORWARDING_TO_IDC)
                        .p(LogFieldConstants.Code, 0);
                if (message.getHeader().getMethId() == ProtoMethodId.Ping.getValue() ||
                        message.getHeader().getMethId() == ProtoMethodId.Pong.getValue()) {
                    kvLogger.d();
                } else {
                    kvLogger.i();
                }
                if (kvLogger.isDebug()) {
                    kvLogger.p(LogFieldConstants.ReqData, Base64.encode(message.toByteArray()))
                            .d();
                }
            }
        });
    }

    public void destroy() {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.BUSINESS)
                .p(LogFieldConstants.ACTION, EdgeEvent.Action.FORWARDING_NODE_DESTROY)
                .p("NodeId", this.nodeId)
                .p(HostStackConstants.CHANNEL_ID, this.context.channel().id())
                .i();
        if (context.channel().isOpen()) {
            context.close();
        }
        if (checkHbScheduler != null) {
            checkHbScheduler.shutdown();
        }
    }
}
