package org.yx.hoststack.center.service.center;

import io.netty.channel.Channel;
import org.yx.hoststack.center.common.dto.ServiceDetailDTO;
import org.yx.hoststack.center.common.req.channel.SendChannelBasic;
import org.yx.hoststack.center.common.req.channel.SendChannelReq;
import org.yx.lib.utils.util.R;

import java.util.Map;

public interface CenterService {
    Channel findLocalChannel(SendChannelBasic request);

    R<?> sendMsgToLocalChannel(SendChannelReq request);

    R<?> sendMsgToLocalOrRemoteChannel(SendChannelReq request);

    R<?> fetchChannelFromRemote(SendChannelReq request);

    String buildRemoteUrl(SendChannelBasic sendChannelBasic, String url);

    Map<String, String> prepareRequestHeaders();

    ServiceDetailDTO findNode(SendChannelBasic request);

    boolean isLocal(String hostIp);
}