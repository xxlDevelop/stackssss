package org.yx.hoststack.edge.client.controller;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.common.syscode.EdgeSysCode;
import org.yx.hoststack.edge.client.EdgeClient;
import org.yx.hoststack.edge.client.EdgeClientConnector;
import org.yx.hoststack.edge.client.controller.manager.EdgeClientControllerManager;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.config.EdgeCommonConfig;
import org.yx.hoststack.edge.cache.IdcConfigCacheManager;
import org.yx.hoststack.edge.cache.RegionConfigCacheManager;
import org.yx.hoststack.edge.server.RunMode;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.SpringContextHolder;

import java.util.concurrent.TimeUnit;

/**
 * Process Center Basic Message
 */
@Service
@RequiredArgsConstructor
public class EdgeBasicMessageController {
    {
        EdgeClientControllerManager.add(ProtoMethodId.EdgeRegister, this::edgeRegisterResult);
        EdgeClientControllerManager.add(ProtoMethodId.Pong, this::pong);
        EdgeClientControllerManager.add(ProtoMethodId.EdgeConfigSync, this::edgeConfigSync);
        EdgeClientControllerManager.add(ProtoMethodId.RegionConfigSync, this::regionConfigSync);
    }

    private final EdgeCommonConfig edgeCommonConfig;
    private final IdcConfigCacheManager idcConfigCacheManager;
    private final RegionConfigCacheManager regionConfigCacheManager;

    /**
     * Edge Register Center Success Result
     *
     * @param ctx     ChannelHandlerContext
     * @param message CommonMessage
     */
    private void edgeRegisterResult(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message) {
        if (message.getBody().getCode() == R.ok().getCode()) {
            KvLogger kvLogger = KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.Business)
                    .p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId())
                    .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                    .p(HostStackConstants.METH_ID, message.getHeader().getMethId());
            C2EMessage.C2E_EdgeRegisterResp edgeRegisterResult;
            try {
                edgeRegisterResult = C2EMessage.C2E_EdgeRegisterResp.parseFrom(message.getBody().getPayload());
            } catch (InvalidProtocolBufferException e) {
                kvLogger.p(LogFieldConstants.ACTION, EdgeEvent.Action.EdgeRegisterFailed)
                        .p(LogFieldConstants.ERR_MSG, e.getMessage())
                        .e(e);
                EdgeClientConnector.getInstance().sendResultToUpstream(message.getHeader().getMethId(),
                        EdgeSysCode.PortoParseException.getValue(), EdgeSysCode.PortoParseException.getMsg(), ByteString.EMPTY, message.getHeader().getTraceId());
                return;
            }
            // set context info
            if (edgeCommonConfig.getRunMode().equals(RunMode.IDC)) {
                EdgeContext.IdcId = edgeRegisterResult.getId();
            } else {
                EdgeContext.RelayId = edgeRegisterResult.getId();
            }
            EdgeContext.Zone = message.getHeader().getZone();
            EdgeContext.Region = message.getHeader().getRegion();
            kvLogger.p(LogFieldConstants.EVENT, EdgeEvent.Business)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.EdgeRegisterSuccessful)
                    .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                    .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                    .p(HostStackConstants.REGION, EdgeContext.Region)
                    .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode)
                    .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                    .i();
            EdgeClientConnector.getInstance().startHb(edgeRegisterResult.getHbInterval());
        } else {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, EdgeEvent.Business)
                    .p(LogFieldConstants.ACTION, EdgeEvent.Action.EdgeRegisterFailed)
                    .p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId())
                    .p(LogFieldConstants.Code, message.getBody().getCode())
                    .p(LogFieldConstants.ERR_MSG, message.getBody().getMsg())
                    .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                    .e();
            try {
                TimeUnit.SECONDS.sleep(5);
                SpringContextHolder.getBean(EdgeClient.class).reConnect();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Center Pong
     *
     * @param ctx     ChannelHandlerContext
     * @param message CommonMessage
     */
    private void pong(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message) {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.Business)
                .p(LogFieldConstants.ACTION, EdgeEvent.Action.CenterPont)
                .p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId())
                .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                .d();
    }

    /**
     * Center sync edge basic and netConfig
     *
     * @param context ChannelHandlerContext
     * @param message CommonMessage
     */
    private void edgeConfigSync(ChannelHandlerContext context, CommonMessageWrapper.CommonMessage message) {
        KvLogger kvLogger = KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.Business)
                .p(LogFieldConstants.ACTION, EdgeEvent.Action.EdgeConfigSync)
                .p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId())
                .p(HostStackConstants.CHANNEL_ID, context.channel().id())
                .p(HostStackConstants.METH_ID, message.getHeader().getMethId());
        C2EMessage.C2E_EdgeConfigSyncReq eEdgeConfigSyncReq;
        try {
            eEdgeConfigSyncReq = C2EMessage.C2E_EdgeConfigSyncReq.parseFrom(message.getBody().getPayload());
        } catch (InvalidProtocolBufferException e) {
            kvLogger.p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .e(e);
            EdgeClientConnector.getInstance().sendResultToUpstream(message.getHeader().getMethId(),
                    EdgeSysCode.PortoParseException.getValue(), EdgeSysCode.PortoParseException.getMsg(), ByteString.EMPTY, message.getHeader().getTraceId());
            return;
        }
        kvLogger.p("Config", eEdgeConfigSyncReq.toString())
                .i();
        idcConfigCacheManager.setIdcBasicConfig(eEdgeConfigSyncReq.getBasic());
        idcConfigCacheManager.setIdcNetConfig(eEdgeConfigSyncReq.getNetList());
        EdgeClientConnector.getInstance().sendResultToUpstream(message.getHeader().getMethId(), 0, "", ByteString.empty(), message.getHeader().getTraceId());
    }

    /**
     * Center sync region config
     *
     * @param context ChannelHandlerContext
     * @param message CommonMessage
     */
    private void regionConfigSync(ChannelHandlerContext context, CommonMessageWrapper.CommonMessage message) {
        KvLogger kvLogger = KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.Business)
                .p(LogFieldConstants.ACTION, EdgeEvent.Action.RegionConfigSync)
                .p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId())
                .p(HostStackConstants.CHANNEL_ID, context.channel().id())
                .p(HostStackConstants.METH_ID, message.getHeader().getMethId());
        C2EMessage.C2E_RegionConfigSyncReq regionConfigSyncReq;
        try {
            regionConfigSyncReq = C2EMessage.C2E_RegionConfigSyncReq.parseFrom(message.getBody().getPayload());
        } catch (InvalidProtocolBufferException e) {
            kvLogger.p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .e(e);
            EdgeClientConnector.getInstance().sendResultToUpstream(message.getHeader().getMethId(),
                    EdgeSysCode.PortoParseException.getValue(), EdgeSysCode.PortoParseException.getMsg(), ByteString.EMPTY, message.getHeader().getTraceId());
            return;
        }
        regionConfigCacheManager.setRegionConfig(regionConfigSyncReq);
        EdgeClientConnector.getInstance().sendResultToUpstream(message.getHeader().getMethId(), 0, "", ByteString.empty(), message.getHeader().getTraceId());
    }
}
