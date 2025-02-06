package org.yx.hoststack.edge.client.controller;

import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.common.syscode.EdgeSysCode;
import org.yx.hoststack.edge.client.EdgeClientConnector;
import org.yx.hoststack.edge.client.controller.manager.EdgeClientControllerManager;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.E2CMessage;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ForwardingController {
    {
        EdgeClientControllerManager.add(ProtoMethodId.ForwardingFailed, this::forwardingFailed);
    }

    private void forwardingFailed(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage commonMessage) {
        KvLogger kvLogger = KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.FORWARDING_PROTOCOL)
                .p(LogFieldConstants.ACTION, EdgeEvent.Action.FORWARDING_MSG_FAILED)
                .p(LogFieldConstants.Code, commonMessage.getBody().getCode())
                .p(LogFieldConstants.ERR_MSG, commonMessage.getBody().getMsg())
                .p(HostStackConstants.IDC_SID, commonMessage.getHeader().getIdcSid())
                .p(HostStackConstants.RELAY_SID, commonMessage.getHeader().getRelaySid())
                .p(HostStackConstants.METH_ID, commonMessage.getHeader().getMethId())
                .p(HostStackConstants.CHANNEL_ID, ctx.channel().id())
                .p(HostStackConstants.RUN_MODE, EdgeContext.RunMode);
        try {
            E2CMessage.ForwardFailedNotify forwardFailedNotify =
                    E2CMessage.ForwardFailedNotify.parseFrom(commonMessage.getBody().getPayload());
            kvLogger.p("ForwardingMethId", forwardFailedNotify.getMethId()).w();
            // forward fail to retry
            if (commonMessage.getBody().getCode() == EdgeSysCode.UpstreamServiceNotAvailable.getValue()) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException ignored) {
                }
                EdgeClientConnector.getInstance().disConnect();
            }
        } catch (InvalidProtocolBufferException e) {
            kvLogger.e(e);
        }
    }
}
