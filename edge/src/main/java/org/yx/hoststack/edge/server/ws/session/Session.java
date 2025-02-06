package org.yx.hoststack.edge.server.ws.session;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public abstract class Session {
    private final SessionEventPublisher eventPublisher;
    protected final Map<String, Object> attrs;
    protected final String sessionId;
    protected final SessionType sessionType;
    protected final int sessionTimeout;
    protected final String zone;
    protected final String region;
    protected final long createTime;
    protected final AtomicLong lastUpdateHbAt;
    protected final ChannelHandlerContext context;
    protected final HashedWheelTimer hashedWheelTimer;
    private Timeout timeout;

    public Session(ChannelHandlerContext context, String sessionId, int sessionTimeout,
                   SessionType sessionType, HashedWheelTimer hashedWheelTimer) {
        this.createTime = System.currentTimeMillis();
        this.lastUpdateHbAt = new AtomicLong(createTime);
        this.sessionId = sessionId;
        this.context = context;
        this.sessionTimeout = sessionTimeout;
        this.sessionType = sessionType;

        this.hashedWheelTimer = hashedWheelTimer;

        this.zone = EdgeContext.Zone;
        this.region = EdgeContext.Region;

        eventPublisher = SpringContextHolder.getBean(SessionEventPublisher.class);
        attrs = Maps.newConcurrentMap();
    }

    public void tick() {
        lastUpdateHbAt.set(System.currentTimeMillis());
        timeout.cancel();
        timeout = hashedWheelTimer.newTimeout(new SessionTimeoutTask(this), sessionTimeout, TimeUnit.SECONDS);
    }

    protected void startTimeoutCheck() {
        if (timeout != null) {
            timeout.cancel();
        }
        timeout = hashedWheelTimer.newTimeout(new SessionTimeoutTask(this), sessionTimeout, TimeUnit.SECONDS);
    }

    public void destroy() {
        if (context != null && context.channel() != null && context.channel().isActive()) {
            context.channel().close();
        }
        if (timeout != null) {
            timeout.cancel();
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
                .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_SERVER)
                .p(HostStackConstants.CHANNEL_ID, context.channel().id())
                .p(HostStackConstants.TRACE_ID, agentMessage.getTraceId())
                .p(HostStackConstants.METH_ID, agentMessage.getMethod())
                .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                .p(HostStackConstants.AGENT_ID, this.getAttr(SessionAttrKeys.AgentId));
        if (!canWrite()) {
            kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.SEND_MSG_FAILED)
                    .p(LogFieldConstants.ERR_MSG, "Channel not active")
                    .p(LogFieldConstants.ReqData, agentMessage.toString())
                    .e();
            Optional.ofNullable(failCallback).ifPresent(SendMsgCallback::callback);
            return;
        }
        ChannelFuture channelFuture = context.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(agentMessage)));
        channelFuture.addListener(future -> {
            if (future.isDone() && future.cause() != null) {
                kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.SEND_MSG_FAILED)
                        .p(LogFieldConstants.ERR_MSG, future.cause().getMessage())
                        .p(LogFieldConstants.ReqData, agentMessage.toString())
                        .e(future.cause());
                SessionReSendMap.putResendMessage(context.channel(),
                        SessionReSendData.builder()
                                .centerMethId(centerMethId)
                                .message(agentMessage)
                                .build());
            } else if (future.isDone() && future.isSuccess()) {
                kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.SEND_MSG_SUCCESSFUL)
                        .i();
                if (kvLogger.isDebug()) {
                    kvLogger.p(LogFieldConstants.ReqData, agentMessage.toString())
                            .d();
                }
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

    private class SessionTimeoutTask implements TimerTask {
        private final Session session;

        public SessionTimeoutTask(Session session) {
            this.session = session;
        }

        @Override
        public void run(Timeout timeout) {
            eventPublisher.publishCustomEvent(new SessionTimeoutEvent(session.getSource(), null));
        }
    }
}
