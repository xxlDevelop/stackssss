package org.yx.hoststack.center.ws.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.List;

/**
 *
 * packageName org.yx.hoststack.center.ws.handler
 * @author YI-JIAN-ZHANG
 * @version JDK 8
 * @className SendWsErrorMessageHandler
 * @date 2025/1/21
 */
@RequiredArgsConstructor
@Service
public class SendWsErrorMessageHandler {
    /**
     *
     * send error response
     * @author yijian

     * @date 2024/12/17 10:44
     */
    public void sendErrorResponse(ChannelHandlerContext ctx, Integer methId, String zone, String region, String idcSid, String relaySid, String traceId, int code, String msg) {
        CommonMessageWrapper.CommonMessage errorMessage = CommonMessageWrapper.CommonMessage.newBuilder()
                .setHeader(CommonMessageWrapper.CommonMessage.newBuilder().getHeaderBuilder()
                        .setMethId(methId)
                        .setLinkSide(CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE)
                        .setProtocolVer(CommonMessageWrapper.ENUM_PROTOCOL_VER.PROTOCOL_V1_VALUE)
                        .setZone(zone)
                        .setRegion(region)
                        .setIdcSid(idcSid)
                        .setRelaySid(relaySid)
                        .setTraceId(traceId)
                        .build())
                .setBody(CommonMessageWrapper.Body.newBuilder().setCode(code).setMsg(msg).build()).build();
        ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(errorMessage.toByteArray())));
    }


    public void sendSuccessResponse(ChannelHandlerContext ctx, Integer methId, String zone, String region, String idcSid, String relaySid, String traceId, String edgeId, Integer serverHbInterval) {
        CommonMessageWrapper.CommonMessage returnMessage = CommonMessageWrapper.CommonMessage.newBuilder()
                .setHeader(CommonMessageWrapper.CommonMessage.newBuilder()
                        .getHeaderBuilder()
                        .setMethId(methId)
                        .setLinkSide(CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE)
                        .setProtocolVer(CommonMessageWrapper.ENUM_PROTOCOL_VER.PROTOCOL_V1_VALUE)
                        .setZone(zone)
                        .setRegion(region)
                        .setIdcSid(idcSid)
                        .setRelaySid(relaySid)
                        .setTraceId(traceId))
                .setBody(CommonMessageWrapper.Body.newBuilder().setCode(0)
                        .setPayload(
                                C2EMessage.C2E_EdgeRegisterResp.newBuilder()
                                        .setId(edgeId)
                                        .setHbInterval(serverHbInterval)
                                        .build().toByteString()
                        ).build()).build();

        ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(returnMessage.toByteArray())));
    }
}
