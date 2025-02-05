package org.yx.hoststack.edge.transfer;

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.edge.client.EdgeClientConnector;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.common.KvMappingChannelContextTempData;
import org.yx.hoststack.edge.transfer.manager.RelayControllerManager;
import org.yx.hoststack.edge.transfer.manager.TransferNode;
import org.yx.hoststack.edge.transfer.manager.TransferNodeMgr;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;

@Service
@RequiredArgsConstructor
public class RelayTransferController {
    {
        RelayControllerManager.add(ProtoMethodId.TransferToCenter, this::transferToCenter);
        RelayControllerManager.add(ProtoMethodId.TransferToIdc, this::transferToIdc);
    }

    @Value("${idc.sessionTimeout:10}")
    private int idcSessionTimeout;

    private final KvMappingChannelContextTempData kvMappingChannelContextTempData;
    private final TransferNodeMgr transferNodeMgr;

    private void transferToCenter(ChannelHandlerContext context, CommonMessageWrapper.CommonMessage message) {
        if (message.getHeader().getLinkSide() == CommonMessageWrapper.ENUM_LINK_SIDE.EDGE_TO_CENTER_VALUE) {
            // replay transfer center, if message is idc register, hold this message, prepare create idc session to use
            if (message.getHeader().getMethId() == ProtoMethodId.EdgeRegister.getValue()) {
                kvMappingChannelContextTempData.put(message.getHeader().getIdcSid(), context);
            }
            EdgeClientConnector.getInstance().sendMsg(buildRelaySendMessage(message), null, null);
        }
    }

    private void transferToIdc(ChannelHandlerContext context, CommonMessageWrapper.CommonMessage message) {
        if (message.getHeader().getLinkSide() == CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE) {
            if (message.getHeader().getMethId() == ProtoMethodId.EdgeRegister.getValue()) {
                try {
                    C2EMessage.C2E_EdgeRegisterResp registerResp = C2EMessage.C2E_EdgeRegisterResp.parseFrom(message.getBody().getPayload());
                    ChannelHandlerContext idcChannelContext = kvMappingChannelContextTempData.get(message.getHeader().getIdcSid());
                    if (idcChannelContext == null) {
                        //TODO
                        return;
                    }
                    TransferNode transferNode = new TransferNode(message.getHeader().getIdcSid(), context, idcSessionTimeout, registerResp.getHbInterval());
                    if (message.getBody().getCode() == R.ok().getCode()) {
                        // create idc transfer node
                        transferNodeMgr.addTransferNode(transferNode);
                        KvLogger.instance(this)
                                .p(LogFieldConstants.EVENT, EdgeEvent.Business)
                                .p(LogFieldConstants.ACTION, EdgeEvent.Action.CreateIdcServiceSession)
                                .p(HostStackConstants.CHANNEL_ID, context.channel().id())
                                .p(HostStackConstants.TRACE_ID, message.getHeader().getTraceId())
                                .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                                .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                                .i();
                    } else {
                        KvLogger.instance(this)
                                .p(LogFieldConstants.EVENT, EdgeEvent.Business)
                                .p(LogFieldConstants.ACTION, EdgeEvent.Action.TransferToIdc)
                                .p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId())
                                .p(LogFieldConstants.Code, message.getBody().getCode())
                                .p(LogFieldConstants.ERR_MSG, message.getBody().getMsg())
                                .p(HostStackConstants.CHANNEL_ID, idcChannelContext.channel().id())
                                .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                                .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                                .e();
                    }
                    transferNode.sendMsg(buildRelaySendMessage(message));
                    kvMappingChannelContextTempData.remove(message.getHeader().getIdcSid());
                } catch (InvalidProtocolBufferException e) {
                    KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, EdgeEvent.TransferProtocol)
                            .p(LogFieldConstants.ACTION, EdgeEvent.Action.TransferToIdc)
                            .p(HostStackConstants.METH_ID, ProtoMethodId.EdgeRegister.getValue())
                            .p(HostStackConstants.TRACE_ID, message.getHeader().getTraceId())
                            .p(HostStackConstants.IDC_SID, EdgeContext.IdcServiceId)
                            .p(HostStackConstants.RELAY_SID, EdgeContext.RelayServiceId)
                            .e(e);
                }
            } else {
                TransferNode transferNode = transferNodeMgr.get(message.getHeader().getIdcSid());
                if (message.getHeader().getMethId() == ProtoMethodId.Ping.getValue()) {
                    transferNode.hb();
                }
                transferNode.sendMsg(buildRelaySendMessage(message));
            }
        }
    }

    private CommonMessageWrapper.CommonMessage buildRelaySendMessage(CommonMessageWrapper.CommonMessage idcMessage) {
        return CommonMessageWrapper.CommonMessage.newBuilder()
                .setHeader(CommonMessageWrapper.CommonMessage.newBuilder().getHeaderBuilder()
                        .setLinkSide(idcMessage.getHeader().getLinkSide())
                        .setProtocolVer(idcMessage.getHeader().getProtocolVer())
                        .setZone(idcMessage.getHeader().getZone())
                        .setRegion(idcMessage.getHeader().getRegion())
                        .setRelaySid(EdgeContext.RelayServiceId)
                        .setIdcSid(idcMessage.getHeader().getIdcSid())
                        .setMethId(idcMessage.getHeader().getMethId())
                        .setTraceId(idcMessage.getHeader().getTraceId()))
                .setBody(idcMessage.getBody())
                .build();
    }
}
