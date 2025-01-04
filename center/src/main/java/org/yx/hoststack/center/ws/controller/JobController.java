package org.yx.hoststack.center.ws.controller;

import com.google.protobuf.ByteString;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.ws.controller.manager.CenterControllerManager;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.E2CMessage;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;

import static org.yx.hoststack.center.common.enums.SysCode.x00000500;


/**
 * Process Center Basic Message
 */
@Service
@RequiredArgsConstructor
public class JobController {
    {
        CenterControllerManager.add(ProtoMethodId.JobReport, this::jobReport);
    }
    /**
     * Edge Register Center Success Result
     *
     * @param ctx     ChannelHandlerContext
     * @param message CommonMessage
     */
    private void jobReport(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message) {
        try {
            ByteString payload = message.getBody().getPayload();
            E2CMessage.E2C_JobReportReq jobReportReq = E2CMessage.E2C_JobReportReq.parseFrom(payload);

            CommonMessageWrapper.CommonMessage returnMessage = CommonMessageWrapper.CommonMessage.newBuilder()
                    .setHeader(CommonMessageWrapper.CommonMessage.newBuilder()
                            .getHeaderBuilder()
                            .setMethId(message.getHeader().getMethId())
                            .setLinkSide(CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE)
                            .setProtocolVer(CommonMessageWrapper.ENUM_PROTOCOL_VER.PROTOCOL_V1_VALUE)
                            .setIdcSid(message.getHeader().getIdcSid())
                            .setZone(message.getHeader().getZone())
                            .setRegion(message.getHeader().getRegion())
                            .setIdcSid(message.getHeader().getIdcSid())
                            .setMethId(message.getHeader().getMethId())
                            .setTraceId(message.getHeader().getTraceId()))
                    .setBody(CommonMessageWrapper.Body.newBuilder().setCode(0).setMsg("successful").build()).build();

            ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(returnMessage.toByteArray())));
        } catch (Exception ex) {
            sendErrorResponse(ctx, message, x00000500.getValue(), x00000500.getMsg());
        }
    }


    /**
     *
     * send error response
     * @author yijian

     * @date 2024/12/17 10:44
     */
    private void sendErrorResponse(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message, int code, String msg) {
        CommonMessageWrapper.CommonMessage errorMessage =
                CommonMessageWrapper.CommonMessage.newBuilder().setHeader(message.getHeader().toBuilder()
                                .setLinkSide(CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE))
                        .setBody(CommonMessageWrapper.Body.newBuilder().setCode(code).setMsg(msg).build()).build();
        ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(errorMessage.toByteArray())));
    }
}
