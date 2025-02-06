package org.yx.hoststack.edge.server.ws.handler;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
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
import org.yx.hoststack.edge.server.RunMode;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.token.YxTokenBuilderUtil;
import org.yx.lib.utils.util.StringPool;
import org.yx.lib.utils.util.StringUtil;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EdgeServerHandshakeProcessHandler implements ChannelHandler {

    //    private final ChannelManager channelManager;

    @Override
    public void doHandle(ChannelHandlerContext ctx, Object msg) {
        try {
            FullHttpRequest req = ((FullHttpRequest) msg);
            String xForwardedFor = req.headers().get("X-Forwarded-For");
            String clientIp = xForwardedFor != null ? xForwardedFor : ctx.channel().remoteAddress().toString();

            String xToken;
            String connectType;
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(req.uri());
            Map<String, List<String>> params = queryStringDecoder.parameters();
            List<String> paramList = params.get("x-token");
            if (paramList != null && !paramList.isEmpty()) {
                xToken = paramList.getFirst();
            } else {
                xToken = "";
            }
            connectType = req.headers().get("connectType");
            if (StringUtil.isBlank(connectType)) {
                connectType = "agent";
            }

            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_SERVER)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.PREPARE_HANDSHAKE)
                    .p(LogFieldConstants.X_Token, xToken)
                    .p(HostStackConstants.CLIENT_IP, clientIp)
                    .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                    .p("ContentType", connectType)
                    .i();

            if (req.decoderResult().isFailure() && req.decoderResult().cause() != null) {
                KvLogger.instance(this)
                        .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_SERVER)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.PREPARE_HANDSHAKE)
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
                        .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_SERVER)
                        .p(LogFieldConstants.ACTION, EdgeEvent.Action.PREPARE_HANDSHAKE)
                        .p(LogFieldConstants.Code, 400)
                        .p(LogFieldConstants.ERR_MSG, "Request is not ws or wss")
                        .p(HostStackConstants.CLIENT_IP, clientIp)
                        .p(HttpHeaders.UPGRADE, req.headers().get(HttpHeaders.UPGRADE))
                        .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                        .w();
                wsProtocolDecodeUpgradeFailed(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }
            if (!authXToken(ctx, xToken, connectType, clientIp)) {
                wsProtocolDecodeUpgradeFailed(ctx, HttpResponseStatus.UNAUTHORIZED);
                return;
            }
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    "ws:/".concat(ctx.channel().id().toString()), null, true);
            WebSocketServerHandshaker handShaker = wsFactory.newHandshaker(req);
            if (handShaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                String finalConnectType = connectType;
                handShaker.handshake(ctx.channel(), req).addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        future.channel().attr(AttributeKey.valueOf(HostStackConstants.X_TOKEN)).set(xToken);
                        future.channel().attr(AttributeKey.valueOf(HostStackConstants.CLIENT_IP)).set(clientIp);
                        KvLogger.instance(this)
                                .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_SERVER)
                                .p(LogFieldConstants.ACTION, EdgeEvent.Action.HANDSHAKE_SUCCESSFUL)
                                .p(HostStackConstants.CLIENT_IP, clientIp)
                                .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                                .p("ConnectType", finalConnectType)
                                .i();
                    }
                });
            }
        } catch (Exception ex) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_SERVER)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.PREPARE_HANDSHAKE)
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

    private boolean authXToken(ChannelHandlerContext context, String xToken, String connectType, String clientIp) {
        if (connectType.equalsIgnoreCase(RunMode.IDC)) {
            return true;
        } else {
            // forTest begin
            String ak1 = "pkl4rq2x5ss2voqsv9ya";
            String sk = "8cgwv3t9oiqtgck0zgk8k22oprxpswiwlc2is8deosqlsdy52f71heh1ymcwajeu";
            Long xTokenExpireS = 36000L;
            Map<String, Object> customerTokenPayload = Maps.newHashMap();
            customerTokenPayload.put("uid", 1111111111);
            xToken = YxTokenBuilderUtil.buildXToken(ak1, sk, xTokenExpireS, customerTokenPayload);
            // forTest end

            KvLogger kvLogger = KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.EDGE_WS_SERVER)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.X_TOKEN_VALID)
                    .p("XToken", xToken)
                    .p(HostStackConstants.CLIENT_IP, clientIp)
                    .p(HostStackConstants.CHANNEL_ID, context.channel().id());
            if (StringUtil.isBlank(xToken)) {
                kvLogger.p(LogFieldConstants.ERR_MSG, "XToken is empty").w();
                return false;
            }
            List<String> tokens = StrUtil.split(xToken, StringPool.DOT);
            if (tokens.size() != 3) {
                kvLogger.p(LogFieldConstants.ERR_MSG, "XToken illegality").w();
                return false;
            }
            try {
                JSONObject payload = JSON.parseObject(Base64.getDecoder().decode(tokens.get(1)));
                Long aLong = payload.getLong("exp");
                if (aLong == null) {
                    kvLogger.p(LogFieldConstants.ERR_MSG, "XToken exp is null").w();
                    return false;
                }
                if (aLong < System.currentTimeMillis()) {
                    kvLogger.p(LogFieldConstants.ERR_MSG, "XToken is expire").w();
                    return false;
                }
                String nonce = payload.getString("nonce");
                String ak = payload.getString("ak");
                if (StringUtil.isBlank(nonce) || StringUtil.isBlank(ak)) {
                    kvLogger.p(LogFieldConstants.ERR_MSG, "XToken nonce or ak is empty").w();
                    return false;
                }
                byte[] decodeHeaders = Base64.getDecoder().decode(tokens.getFirst());
                JSONObject headers = JSON.parseObject(decodeHeaders);
                String alg = headers.getString("alg");
                String typ = headers.getString("typ");
                if (StringUtil.isBlank(alg) || StringUtil.isBlank(typ)) {
                    kvLogger.p(LogFieldConstants.ERR_MSG, "XToken alg or typ is empty").w();
                    return false;
                }
                if (!"HS256".equalsIgnoreCase(alg) || !"JWT".equalsIgnoreCase(typ)) {
                    kvLogger.p(LogFieldConstants.ERR_MSG, "XToken alg or typ illegality").w();
                    return false;
                }
            } catch (Exception ex) {
                kvLogger.p(LogFieldConstants.ERR_MSG, ex.getMessage()).e(ex);
                return false;
            }
        }
        return true;
    }
}

