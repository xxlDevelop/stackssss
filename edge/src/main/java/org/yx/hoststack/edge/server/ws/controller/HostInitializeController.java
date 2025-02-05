package org.yx.hoststack.edge.server.ws.controller;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.common.syscode.EdgeSysCode;
import org.yx.hoststack.edge.client.EdgeClientConnector;
import org.yx.hoststack.edge.common.EdgeEvent;
import org.yx.hoststack.edge.common.KvMappingChannelContextTempData;
import org.yx.hoststack.edge.server.ws.controller.manager.EdgeServerControllerManager;
import org.yx.hoststack.protocol.ws.agent.common.CommonMessage;
import org.yx.hoststack.protocol.ws.agent.common.AgentMethodId;
import org.yx.hoststack.protocol.ws.agent.common.MessageType;
import org.yx.hoststack.protocol.ws.agent.req.HostInitializeReq;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

@Service
@RequiredArgsConstructor
public class HostInitializeController extends BaseController {
    {
        EdgeServerControllerManager.add(AgentMethodId.Initialize, this::hostInitialize);
    }

    private final KvMappingChannelContextTempData kvMappingChannelContextTempData;

    private void hostInitialize(ChannelHandlerContext context, CommonMessage<?> commonMessage) {
        HostInitializeReq hostInitializeReq = ((JSONObject) commonMessage.getData()).toJavaObject(HostInitializeReq.class);
        // set channel attr
        context.channel().attr(AttributeKey.valueOf(HostStackConstants.HOST_TYPE)).set(hostInitializeReq.getAgentType());
        // save agent devSn & channel mapping relationship
        kvMappingChannelContextTempData.put(hostInitializeReq.getDevSn(), context);
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, EdgeEvent.EdgeWsServer)
                .p(LogFieldConstants.ACTION, EdgeEvent.Action.HostPrepareInitialize)
                .p("HostData", JSON.toJSONString(hostInitializeReq))
                .i();
        EdgeClientConnector.getInstance().hostInitialize(commonMessage.getHostId(), hostInitializeReq, UUID.fastUUID().toString(), null,
                () -> sendAgentResult(commonMessage.getMethod(), commonMessage.getHostId(), commonMessage.getTraceId(),
                        EdgeSysCode.UpstreamServiceNotAvailable.getValue(), EdgeSysCode.UpstreamServiceNotAvailable.getMsg(),
                        null, context));
    }
}
