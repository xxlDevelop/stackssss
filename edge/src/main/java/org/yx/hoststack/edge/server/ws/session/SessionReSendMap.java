package org.yx.hoststack.edge.server.ws.session;

import io.netty.channel.Channel;
import lombok.Getter;
import org.yx.hoststack.protocol.ws.ResendMessage;

import java.text.MessageFormat;
import java.util.concurrent.ConcurrentHashMap;

public class SessionReSendMap {
    @Getter
    private static final ConcurrentHashMap<String, ResendMessage<SessionReSendData>> data = new ConcurrentHashMap<>();

    public static void putResendMessage(Channel channel, SessionReSendData reSendData) {
        String resendId = MessageFormat.format("{0}-{1}", channel.id(), reSendData.getMessage().getTraceId());
        ResendMessage<SessionReSendData> resendMessage = new ResendMessage<>();
        resendMessage.setReSendId(resendId);
        resendMessage.setData(reSendData);
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
