package org.yx.hoststack.center.ws.session;

import cn.hutool.core.codec.Base64;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.Getter;
import org.yx.hoststack.center.common.CenterEvent;
import org.yx.hoststack.center.common.SendMsgCallback;
import org.yx.hoststack.center.common.enums.RegisterNodeEnum;
import org.yx.hoststack.center.ws.ReSendMap;
import org.yx.hoststack.center.ws.session.event.SessionEventPublisher;
import org.yx.hoststack.center.ws.session.event.SessionTimeoutEvent;
import org.yx.hoststack.protocol.ws.ResendMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.SpringContextHolder;
import org.yx.lib.utils.util.StringPool;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.yx.hoststack.center.ws.CenterServer.address;
import static org.yx.hoststack.center.ws.CenterServer.port;

/**
 * @author zhang
 */
@Getter
public abstract class Session {
    protected final RegisterNodeEnum sessionType;
    protected final ChannelHandlerContext context;
    protected final HashedWheelTimer hashedWheelTimer;
    private final SessionEventPublisher sessionEventPublisher;

    protected String sessionId;
    protected String serviceIp;
    protected String relaySid;
    protected String idcSid;
    protected String nodeId;
    protected String relayId;
    protected String idcId;
    protected String zone;
    protected String region;
    protected String location;
    protected Timeout timeout;
    protected long tenantId;
    protected long sessionTimeout;

    public Session(RegisterNodeEnum sessionType, ChannelHandlerContext context, HashedWheelTimer hashedWheelTimer, long sessionTimeout) {
        this.sessionType = sessionType;
        this.context = context;
        this.hashedWheelTimer = hashedWheelTimer;
        this.sessionTimeout = sessionTimeout;

        this.sessionEventPublisher = SpringContextHolder.getBean(SessionEventPublisher.class);
    }

    public void tick() {
        _tick();
    }

    public abstract R<?> initialize0(CommonMessageWrapper.CommonMessage message) throws Exception;

    public abstract void destroy0();

    public abstract void attr(String attr, String val);

    public abstract String attr(String attr);

    public abstract void removeAttr(String attr);

    public abstract Object getSource();

    public void destroy() {
        System.out.println("Destroying session.");
        context.channel().close();
        if (timeout != null) {
            timeout.cancel();
        }
        removeAttr(SessionAttrKeys.REGISTER_IP_AND_PORT);
        destroy0();
        // TODO call sessionMgr remove session;
    }

    protected void _tick() {
        if (timeout != null) {
            timeout.cancel();
        }
        timeout = hashedWheelTimer.newTimeout(new SessionTimeoutTask(this), sessionTimeout, TimeUnit.SECONDS);
    }

    public R<?> initialize(CommonMessageWrapper.CommonMessage message) throws Exception {
        R<?> initR = initialize0(message);
        if (initR.getCode() == R.ok().getCode()) {
            //RedissonUtils.setLocalCachedMap(serverConsistentHash.getShard(this.getSessionId()).toString(), this.getSessionId(), address + ":" + port);
            attr(SessionAttrKeys.REGISTER_IP_AND_PORT, address + StringPool.COLON + port);
            _tick();
        }
        return initR;
    }

    public boolean canWrite() {
        return context.channel().isActive() && context.channel().isOpen() && context.channel().isWritable();
    }

    public void sendMsg(CommonMessageWrapper.CommonMessage message, SendMsgCallback successCallback, SendMsgCallback failCallback) {
        if (!canWrite()) {
            KvLogger.instance(this).p(LogFieldConstants.ACTION, CenterEvent.Action.SEND_MSG_FAILED)
                    .p(LogFieldConstants.ERR_MSG, "Channel is not active")
                    .p(LogFieldConstants.ReqData, message.getBody().toString())
                    .e();
            Optional.ofNullable(failCallback).ifPresent(SendMsgCallback::callback);
            return;
        }

        byte[] protobufMessage = message.toByteArray();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(protobufMessage);
        ChannelFuture channelFuture = context.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
        channelFuture.addListener(future -> {
            if (future.isDone() && future.cause() != null) {
//                kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.SEND_MSG_FAILED)
//                        .p(LogFieldConstants.ERR_MSG, future.cause().getMessage())
//                        .p(LogFieldConstants.ReqData, agentMessage.toString())
//                        .e(future.cause());
                String resendId = MessageFormat.format("{0}-{1}", context.channel().id(), message.getHeader().getTraceId());
                ResendMessage<String> resendMessage = new ResendMessage<>();
                resendMessage.setReSendId(resendId);
                resendMessage.setData(message.toString());
                resendMessage.setChannel(context.channel());

                ReSendMap.DATA.put(resendId, resendMessage);
            } else if (future.isDone() && future.isSuccess()) {
                KvLogger.instance(this).p(LogFieldConstants.ACTION, CenterEvent.Action.SEND_MSG_SUCCESS)
                        .i();
                Optional.ofNullable(successCallback).ifPresent(SendMsgCallback::callback);
            }
        });
    }

    public void sendMsg(String traceId, CommonMessageWrapper.Body body, SendMsgCallback successCallback, SendMsgCallback failCallback) {
        if (!canWrite()) {
            KvLogger.instance(this).p(LogFieldConstants.ACTION, CenterEvent.Action.SEND_MSG_FAILED)
                    .p(LogFieldConstants.ERR_MSG, "Channel is not active")
                    .p(LogFieldConstants.ReqData, Base64.encode(body.getPayload().toByteArray()))
                    .e();
            Optional.ofNullable(failCallback).ifPresent(SendMsgCallback::callback);
            return;
        }
        CommonMessageWrapper.CommonMessage message = CommonMessageWrapper.CommonMessage.newBuilder()
                .setHeader(CommonMessageWrapper.Header.newBuilder()
                        .setLinkSide(CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE)
                        .setTraceId(traceId)
                        .setProtocolVer(CommonMessageWrapper.ENUM_PROTOCOL_VER.PROTOCOL_V1_VALUE)
                        .setZone(this.getZone())
                        .setRegion(this.getRegion())
                        .setRelaySid(this.getRelaySid())
                        .setIdcSid(this.getIdcSid())
                        .setTimestamp(System.currentTimeMillis())
                        .setMethId(ProtoMethodId.DoJob.getValue())
                        .setTenantId(this.getTenantId())
                        .build())
                .setBody(body)
                .build();
        byte[] protobufMessage = message.toByteArray();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(protobufMessage);
        ChannelFuture channelFuture = context.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
        channelFuture.addListener(future -> {
            if (future.isDone() && future.cause() != null) {
//                kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.SEND_MSG_FAILED)
//                        .p(LogFieldConstants.ERR_MSG, future.cause().getMessage())
//                        .p(LogFieldConstants.ReqData, agentMessage.toString())
//                        .e(future.cause());
                String resendId = MessageFormat.format("{0}-{1}", context.channel().id(), message.getHeader().getTraceId());
                ResendMessage<String> resendMessage = new ResendMessage<>();
                resendMessage.setReSendId(resendId);
                resendMessage.setData(message.toString());
                resendMessage.setChannel(context.channel());

                ReSendMap.DATA.put(resendId, resendMessage);
            } else if (future.isDone() && future.isSuccess()) {
                KvLogger.instance(this).p(LogFieldConstants.ACTION, CenterEvent.Action.SEND_MSG_SUCCESS)
                        .i();
                Optional.ofNullable(successCallback).ifPresent(SendMsgCallback::callback);
            }
        });
    }

    private class SessionTimeoutTask implements TimerTask {
        private final Session session;

        public SessionTimeoutTask(Session session) {
            this.session = session;
        }

        @Override
        public void run(Timeout timeout) {
//            System.out.println("Session timed out.");
//            session.destroy();
            sessionEventPublisher.publishCustomEvent(new SessionTimeoutEvent(session.getSource(), null));
        }
    }
}
