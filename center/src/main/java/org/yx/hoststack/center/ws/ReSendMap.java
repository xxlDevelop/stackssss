package org.yx.hoststack.center.ws;

import org.yx.hoststack.protocol.ws.ResendMessage;

import java.util.concurrent.ConcurrentHashMap;

public class ReSendMap {
    public static final ConcurrentHashMap<String, ResendMessage<String>> DATA = new ConcurrentHashMap<>();

//    public void putResendMessage(Channel channel, String msg) {
//        String resendId = MessageFormat.format("{0}-{1}", channel.id(), msg.getBody().getMsgId());
//        ResendMessage<String> resendMessage = new ResendMessage<>();
//        resendMessage.setReSendId(resendId);
//        resendMessage.setMessage(msg);
//        DATA.putIfAbsent(resendId, resendMessage);
//    }
}
