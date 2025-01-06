package org.yx.hoststack.edge.forwarding.manager;

import io.netty.channel.Channel;
import lombok.Getter;
import org.yx.hoststack.protocol.ws.ResendMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;

import java.text.MessageFormat;
import java.util.concurrent.ConcurrentHashMap;

public class ForwardingReSendMap {
    @Getter
    private static final ConcurrentHashMap<String, ResendMessage<CommonMessageWrapper.CommonMessage>> data = new ConcurrentHashMap<>();

    public static void putResendMessage(Channel channel, CommonMessageWrapper.CommonMessage msg) {
        String resendId = MessageFormat.format("{0}-{1}", channel.id(), msg.getHeader().getTraceId());
        ResendMessage<CommonMessageWrapper.CommonMessage> resendMessage = new ResendMessage<>();
        resendMessage.setReSendId(resendId);
        resendMessage.setData(msg);
        resendMessage.setChannel(channel);
        data.putIfAbsent(resendId, resendMessage);
    }

    public static void remove(String resendMsgId) {
        data.remove(resendMsgId);
    }

    public static void clear() {
        data.clear();
    }

}
