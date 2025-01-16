package org.yx.hoststack.center.service;

import io.netty.channel.Channel;
import org.yx.hoststack.center.common.req.channel.SendChannelReq;
import org.yx.lib.utils.util.R;

public interface CenterService {
    Channel getChannel(SendChannelReq request);
    Channel findLocalChannel(SendChannelReq request);
    R<?> sendMsgToChannel(SendChannelReq request);
}