package org.yx.hoststack.center.service.center;

import io.netty.channel.Channel;
import org.yx.hoststack.center.common.req.channel.SendChannelReq;
import org.yx.lib.utils.util.R;

public interface CenterService {
    Channel findLocalChannel(SendChannelReq request);

    R<?> sendMsgToLocalChannel(SendChannelReq request);

    R<?> sendMsgToLocalOrRemoteChannel(SendChannelReq request);

    R<?> fetchChannelFromRemote(SendChannelReq request);
}