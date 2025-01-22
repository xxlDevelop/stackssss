package org.yx.hoststack.center.ws.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.entity.ImageInfo;
import org.yx.hoststack.center.service.ImageInfoService;
import org.yx.hoststack.center.ws.controller.manager.CenterControllerManager;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.E2CMessage;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.StringUtil;

import java.sql.Timestamp;

import static org.yx.hoststack.center.common.constant.CenterEvent.Action.JOB_CONTROLLER_FAIL;
import static org.yx.hoststack.center.common.constant.CenterEvent.Action.JOB_CONTROLLER_SAVE_IMAGE_INFO_SUCCESS;
import static org.yx.hoststack.center.common.constant.CenterEvent.JOB_CONTROLLER_EVENT;
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

    private final ImageInfoService imageInfoService;

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

            CommonMessageWrapper.Header header = message.getHeader();
            // Process each job report item
            for (E2CMessage.JobReportItem item : jobReportReq.getItemsList()) {
                processJobReport(item, message);
            }

            CommonMessageWrapper.CommonMessage returnMessage = CommonMessageWrapper.CommonMessage.newBuilder()
                    .setHeader(CommonMessageWrapper.CommonMessage.newBuilder()
                            .getHeaderBuilder()
                            .setMethId(header.getMethId())
                            .setLinkSide(CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE)
                            .setProtocolVer(CommonMessageWrapper.ENUM_PROTOCOL_VER.PROTOCOL_V1_VALUE)
                            .setIdcSid(header.getIdcSid())
                            .setZone(header.getZone())
                            .setRegion(header.getRegion())
                            .setIdcSid(header.getIdcSid())
                            .setMethId(header.getMethId())
                            .setTraceId(header.getTraceId()))
                    .setBody(CommonMessageWrapper.Body.newBuilder().setCode(0).setMsg("successful").build()).build();

            ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(returnMessage.toByteArray())));
        } catch (Exception ex) {
            sendErrorResponse(ctx, message, x00000500.getValue(), x00000500.getMsg());
        }
    }

    private void processJobReport(E2CMessage.JobReportItem item, CommonMessageWrapper.CommonMessage message) {
        try {
            String jobResult = item.getJobResult().toStringUtf8();
            JSONObject resultObj = JSON.parseObject(jobResult);
            JSONArray targetResults = resultObj.getJSONArray("targetResult");

            if (targetResults == null || targetResults.isEmpty()) {
                logJobReportError(item, "Empty target result", message);
                return;
            }

            // Process each target result
            for (int i = 0; i < targetResults.size(); i++) {
                JSONObject target = targetResults.getJSONObject(i);
                JSONObject output = target.getJSONObject("output");

                if (R.ok().getCode() == target.getInteger("code")) {
                    processSuccessResult(item, output, message);
                } else {
                    logJobReportError(item, target.getString("msg"), message);
                }
            }

        } catch (Exception e) {
            logJobReportError(item, "Failed to process job report: " + e.getMessage(), message);
        }
    }

    private void processSuccessResult(E2CMessage.JobReportItem item, JSONObject output, CommonMessageWrapper.CommonMessage message) {
        if (output == null) {
            logJobReportError(item, "Empty output", message);
            return;
        }

        String idcStoragePath = output.getString("idcStoragePath");
        String md5 = output.getString("md5");

        if (StringUtil.isNotBlank(idcStoragePath) && StringUtil.isNotBlank(md5)) {
            try {
                // Update image info with IDC storage path
                ImageInfo imageInfo = imageInfoService.getByMd5(md5);
                if (imageInfo != null) {
                    imageInfo.setStoragePath(idcStoragePath);
                    imageInfo.setIsEnabled(true);
                    imageInfo.setLastUpdateAt(new Timestamp(System.currentTimeMillis()));
                    imageInfo.setLastUpldateAccount("");// TODO: Set last update account

                    if (imageInfoService.updateById(imageInfo)) {
                        // Log success
                        KvLogger.instance(this)
                                .p(LogFieldConstants.EVENT, JOB_CONTROLLER_EVENT)
                                .p(LogFieldConstants.ACTION, JOB_CONTROLLER_SAVE_IMAGE_INFO_SUCCESS)
                                .p("JobId", item.getJobId())
                                .p(LogFieldConstants.TRACE_ID, item.getTraceId())
                                .p("IdcStoragePath", idcStoragePath)
                                .p("ImageId", imageInfo.getImageId())
                                .p("MD5", md5)
                                .p("Region", message.getHeader().getRegion())
                                .p("Idc", message.getHeader().getIdcSid())
                                .i();
                    } else {
                        logJobReportError(item, "Failed to update image info", message);
                    }
                } else {
                    logJobReportError(item, "Image not found with MD5: " + md5, message);
                }
            } catch (Exception e) {
                logJobReportError(item, "Failed to update image info: " + e.getMessage(), message);
            }
        } else {
            logJobReportError(item, "Invalid idcStoragePath or md5", message);
        }
    }

    private void logJobReportError(E2CMessage.JobReportItem item, String errorMsg, CommonMessageWrapper.CommonMessage message) {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, JOB_CONTROLLER_EVENT)
                .p(LogFieldConstants.ACTION, JOB_CONTROLLER_FAIL)
                .p(LogFieldConstants.ERR_MSG, errorMsg)
                .p(LogFieldConstants.Alarm, 0)
                .p("JobId", item.getJobId())
                .p(LogFieldConstants.TRACE_ID, item.getTraceId())
                .p("Region", message.getHeader().getRegion())
                .p("Idc", message.getHeader().getIdcSid())
                .e();
        throw new RuntimeException(errorMsg);
    }


    /**
     * send error response
     *
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
