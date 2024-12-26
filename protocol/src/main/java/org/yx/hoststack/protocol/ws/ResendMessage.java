package org.yx.hoststack.protocol.ws;

import io.netty.channel.Channel;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ResendMessage<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 610633890747696854L;

    private String reSendId;

    /**
     * 通讯通道
     */
    private Channel channel;

    /**
     * 需要重发的消息
     */
    private T data;

    /**
     * 已重发的次数
     */
    private Integer retry = 0;
}