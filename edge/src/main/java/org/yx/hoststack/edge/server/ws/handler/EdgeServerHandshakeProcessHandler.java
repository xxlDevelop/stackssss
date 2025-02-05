package org.yx.hoststack.edge.server.ws.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.config.EdgeServerConfig;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

@Service
@RequiredArgsConstructor
@Slf4j
public class EdgeServerHandshakeProcessHandler implements ChannelHandler {

    //    private final ChannelManager channelManager;
    private final EdgeServerConfig edgeServerConfig;

    @Override
    public void doHandle(ChannelHandlerContext ctx, Object msg) {
        try {
            FullHttpRequest req = ((FullHttpRequest) msg);
            String xForwardedFor = req.headers().get("X-Forwarded-For");
            String clientIp = xForwardedFor != null ? xForwardedFor : ctx.channel().remoteAddress().toString();
            String xToken = req.headers().get("x-token");

            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.PrepareHandshake)
                    .p(LogFieldConstants.X_Token, xToken)
                    .p(HostStackConstants.CLIENT_IP, clientIp)
                    .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                    .i();

            if (req.decoderResult().isFailure() && req.decoderResult().cause() != null) {
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.PrepareHandshake)
                        .p(LogFieldConstants.Code, 500)
                        .p(LogFieldConstants.ERR_MSG, req.decoderResult().cause().getMessage())
                        .p(HttpHeaders.UPGRADE, req.headers().get(HttpHeaders.UPGRADE))
                        .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                        .p(HostStackConstants.CLIENT_IP, clientIp)
                        .e(req.decoderResult().cause());
                wsProtocolDecodeUpgradeFailed(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }
            if (!"websocket".equals(req.headers().get(HttpHeaders.UPGRADE))) {
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.PrepareHandshake)
                        .p(LogFieldConstants.Code, 400)
                        .p(LogFieldConstants.ERR_MSG, "Request is not ws or wss")
                        .p(HostStackConstants.CLIENT_IP, clientIp)
                        .p(HttpHeaders.UPGRADE, req.headers().get(HttpHeaders.UPGRADE))
                        .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                        .w();
                wsProtocolDecodeUpgradeFailed(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }
            if (!authXToken(ctx, xToken, clientIp)) {
                wsProtocolDecodeUpgradeFailed(ctx, HttpResponseStatus.UNAUTHORIZED);
                return;
            }
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    "ws:/".concat(ctx.channel().id().toString()), null, false);
            WebSocketServerHandshaker handShaker = wsFactory.newHandshaker(req);
            if (handShaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handShaker.handshake(ctx.channel(), req).addListener((ChannelFutureListener) future -> {
                    future.channel().attr(AttributeKey.valueOf(HostStackConstants.CLIENT_IP)).set(clientIp);
//                    log.info("client handshake success, clientId:{}, channelId:{}, remoteAddr:{}", clientId, ctx.channel().id(), clientIP);
                    KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                            .p(LogFieldConstants.ACTION, EdgeEvent.Action.HandshakeSuccessful)
                            .p(HostStackConstants.CLIENT_IP, clientIp)
                            .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                            .i();
                });
            }
        } catch (Exception ex) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.PrepareHandshake)
                    .p(LogFieldConstants.ERR_MSG, ex.getMessage())
                    .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                    .e(ex);
            wsProtocolDecodeUpgradeFailed(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void wsProtocolDecodeUpgradeFailed(ChannelHandlerContext ctx, HttpResponseStatus httpResponseStatus) {
        DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus);
        ByteBuf buf = Unpooled.copiedBuffer(httpResponse.status().toString(), CharsetUtil.UTF_8);
        httpResponse.content().writeBytes(buf);
        buf.release();
        ChannelFuture f = ctx.channel().writeAndFlush(httpResponse);
        f.addListener(ChannelFutureListener.CLOSE);
    }

    private boolean authXToken(ChannelHandlerContext context, String xToken, String clientIp) {
        return true;
    }
}

