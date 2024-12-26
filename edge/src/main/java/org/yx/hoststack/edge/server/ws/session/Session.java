package org.yx.hoststack.edge.server.ws.session;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.Getter;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.common.SendMsgCallback;
import org.yx.hoststack.edge.server.ws.session.event.SessionEventPublisher;
import org.yx.hoststack.edge.server.ws.session.event.SessionTimeoutEvent;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.SpringContextHolder;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public abstract class Session {

    private final ScheduledExecutorService checkHbScheduler;
    private final SessionEventPublisher eventPublisher;
    protected final Map<String, Object> attrs;
    protected String sessionId;
    protected SessionType sessionType;
    protected int sessionTimeout;
    protected String zone;
    protected String region;
    protected long createTime;
    protected AtomicLong lastUpdateHbAt;
    protected int hbInterval = 60;
    protected ChannelHandlerContext context;

    public Session(ChannelHandlerContext context, int sessionTimeout, int sessionHbInterval, SessionType sessionType) {
        this.createTime = System.currentTimeMillis();
        this.lastUpdateHbAt = new AtomicLong(createTime);
        this.context = context;
        this.sessionTimeout = sessionTimeout;
        this.hbInterval = sessionHbInterval;
        this.sessionType = sessionType;

        this.zone = EdgeContext.Zone;
        this.region = EdgeContext.Region;

        checkHbScheduler = Executors.newScheduledThreadPool(1,
                ThreadFactoryBuilder.create().setNamePrefix("agentHb-check-" + context.channel().id()).build());
        eventPublisher = SpringContextHolder.getBean(SessionEventPublisher.class);
        attrs = Maps.newConcurrentMap();
    }

    public void tick() {
        lastUpdateHbAt.set(System.currentTimeMillis());
    }

    protected void startTimeoutCheck() {
        checkHbScheduler.scheduleAtFixedRate(() -> {
            long diff = System.currentTimeMillis() - lastUpdateHbAt.get();
            if (diff > sessionTimeout * 1000L) {
                eventPublisher.publishCustomEvent(new SessionTimeoutEvent(getSource(), null));
            }
        }, 5, hbInterval, TimeUnit.SECONDS);
    }

    public void destroy() {
        if (context.channel().isActive()) {
            context.channel().close();
        }
        if (checkHbScheduler != null && !checkHbScheduler.isShutdown()) {
            checkHbScheduler.shutdown();
        }
        attrs.clear();
    }

    protected void initialize0() {
        startTimeoutCheck();
        initialize();
    }

    public abstract void initialize();

    public boolean canWrite() {
        return context.channel().isActive() && context.channel().isOpen() && context.channel().isWritable();
    }

    public abstract Object getSource();

    public void sendMsg(int centerMethId, AgentCommonMessage<?> agentMessage, SendMsgCallback successCallback, SendMsgCallback failCallback) {
        KvLogger kvLogger = KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                .p(HostStackConstants.CHANNEL_ID, context.channel().id())
                .p(HostStackConstants.TRACE_ID, agentMessage.getTraceId())
                .p(HostStackConstants.METH_ID, agentMessage.getMethod())
                .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                .p(HostStackConstants.AGENT_ID, this.getAttr(SessionAttrKeys.AgentId));
        if (!canWrite()) {
            kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.SendMsgFailed)
                    .p(LogFieldConstants.ERR_MSG, "Channel not active")
                    .p(LogFieldConstants.ReqData, agentMessage.toString())
                    .e();
            Optional.ofNullable(failCallback).ifPresent(SendMsgCallback::callback);
            return;
        }
        ChannelFuture channelFuture = context.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(agentMessage)));
        channelFuture.addListener(future -> {
            if (future.isDone() && future.cause() != null) {
                kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.SendMsgFailed)
                        .p(LogFieldConstants.ERR_MSG, future.cause().getMessage())
                        .p(LogFieldConstants.ReqData, agentMessage.toString())
                        .e(future.cause());
                SessionReSendMap.putResendMessage(context.channel(),
                        SessionReSendData.builder()
                                .centerMethId(centerMethId)
                                .message(agentMessage)
                                .build());
            } else if (future.isDone() && future.isSuccess()) {
                kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.SendMsgSuccessful)
                        .i();
                kvLogger.p(LogFieldConstants.ReqData, agentMessage.toString())
                        .d();
                Optional.ofNullable(successCallback).ifPresent(SendMsgCallback::callback);
            }
        });
    }

    public Object getAttr(String attrKey) {
        return attrs.get(attrKey);
    }

    public void setAttr(String attrKey, Object attrValue) {
        attrs.put(attrKey, attrValue);
    }

    public void setAttrs(Map<String, Object> attrs) {
        attrs.putAll(attrs);
    }
}
