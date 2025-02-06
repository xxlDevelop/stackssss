package org.yx.hoststack.edge.server.ws.controller;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import org.yx.hoststack.protocol.ws.agent.common.AgentCommonMessage;
import org.yx.hoststack.protocol.ws.agent.common.MessageType;

public class BaseController {
    public Object getAttr(Channel channel, String attrKey) {
        return channel.attr(AttributeKey.valueOf(attrKey)).get();
    }

    protected void sendAgentResult(String method, String hostId, String traceId, int code, String message, Object data, ChannelHandlerContext clientContext) {
        clientContext.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(
                AgentCommonMessage.builder()
                        .type(MessageType.RESPONSE)
                        .method(method)
                        .hostId(hostId)
                        .traceId(traceId)
                        .code(code)
                        .msg(message)
                        .data(data)
                        .build()
        )));
    }
}
