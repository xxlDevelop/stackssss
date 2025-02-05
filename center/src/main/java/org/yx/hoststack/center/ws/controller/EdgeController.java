package org.yx.hoststack.center.ws.controller;

import cn.hutool.core.lang.UUID;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.ws.controller.manager.CenterControllerManager;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.E2CMessage;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

/**
 * Process Center Basic Message
 */
@Service
@RequiredArgsConstructor
public class EdgeController {
    {
        CenterControllerManager.add(ProtoMethodId.EdgeRegister, this::register);
        CenterControllerManager.add(ProtoMethodId.Ping, this::ping);
    }

    /**
     * Edge Register Center Success Result
     *
     * @param ctx     ChannelHandlerContext
     * @param message CommonMessage
     */
    private void register(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message) {
        try {
            E2CMessage.E2C_EdgeRegisterReq edgeRegister = E2CMessage.E2C_EdgeRegisterReq.parseFrom(message.getBody().getPayload());
            KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CenterWsServer)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.CenterWsServer_EdgeRegisterCenter)
                    .p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId())
                    .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                    .p(HostStackConstants.TRACE_ID, message.getHeader().getTraceId())
                    .p(HostStackConstants.METH_ID, message.getHeader().getMethId())
                    .p("ServiceIp", edgeRegister.getServiceIp())
                    .i();
            // test
            CommonMessageWrapper.CommonMessage returnMessage = CommonMessageWrapper.CommonMessage.newBuilder()
                    .setHeader(CommonMessageWrapper.CommonMessage.newBuilder().getHeaderBuilder()
                            .setMethId(message.getHeader().getMethId())
                            .setLinkSide(CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE)
                            .setProtocolVer(CommonMessageWrapper.ENUM_PROTOCOL_VER.PROTOCOL_V1_VALUE)
                            .setZone(message.getHeader().getZone())
                            .setRegion(message.getHeader().getRegion())
                            .setIdcSid(message.getHeader().getIdcSid())
                            .setMethId(message.getHeader().getMethId())
                            .setTraceId(message.getHeader().getTraceId()))
                    .setBody(CommonMessageWrapper.Body.newBuilder()
                            .setCode(0)
                            .setPayload(C2EMessage.C2E_EdgeRegisterResp.newBuilder()
                                    .setId(UUID.fastUUID().toString())
                                    .build().toByteString())
                            .build())
                    .build();
            byte[] protobufMessage = returnMessage.toByteArray();
            ByteBuf byteBuf = Unpooled.wrappedBuffer(protobufMessage);
            ctx.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Edge ping
     *
     * @param ctx     ChannelHandlerContext
     * @param message CommonMessage
     */
    private void ping(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message) {
        // Test
        CommonMessageWrapper.CommonMessage returnMessage = CommonMessageWrapper.CommonMessage.newBuilder()
                .setHeader(CommonMessageWrapper.CommonMessage.newBuilder().getHeaderBuilder()
                        .setMethId(ProtoMethodId.Pong.getValue())
                        .setLinkSide(CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE)
                        .setProtocolVer(CommonMessageWrapper.ENUM_PROTOCOL_VER.PROTOCOL_V1_VALUE)
                        .setZone(message.getHeader().getZone())
                        .setRegion(message.getHeader().getRegion())
                        .setIdcSid(message.getHeader().getIdcSid())
                        .setTraceId(message.getHeader().getTraceId())).build();
        byte[] protobufMessage = returnMessage.toByteArray();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(protobufMessage);
        ctx.writeAndFlush(new BinaryWebSocketFrame(byteBuf));
    }
}
