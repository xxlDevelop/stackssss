package org.yx.hoststack.center.service.impl;

import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.CenterApplicationRunner;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.redis.util.RedissonUtils;
import org.yx.hoststack.center.common.req.channel.SendChannelReq;
import org.yx.hoststack.center.service.CenterService;
import org.yx.hoststack.center.ws.common.Node;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;

import java.util.Optional;

import static org.yx.hoststack.center.common.enums.SysCode.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CenterServiceImpl implements CenterService {


    @Override
    public Channel getChannel(SendChannelReq request) {
        // 1. First look it up from the local centerNode
        Optional<Channel> localChannel = Optional.ofNullable(findLocalChannel(request));
        return localChannel.orElseGet(() -> fetchChannelFromRemote(request));
    }

    /**
     * Local lookup Channel
     */
    @Override
    public Channel findLocalChannel(SendChannelReq request) {
        try {
            return Node.NODE_MAP.get(request.getServiceId()).getChannel();
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.FIND_LOCAL_CHANNEL_EVENT)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.FIND_LOCAL_CHANNEL_FAILED)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(request))
                    .e(e);
            return null;
        }
    }

    @Override
    public R<?> sendMsgToChannel(SendChannelReq request) {
        // 1. First look it up from the local centerNode
        Optional<Channel> localChannel = Optional.ofNullable(findLocalChannel(request));
        if (localChannel.isPresent()) {
            Channel channel = localChannel.get();
            if (channel.isActive()) {
                ChannelFuture channelFuture = channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(request.getMsg())));
                channelFuture.addListener(future -> {
                    KvLogger kvLogger = KvLogger.instance(this)
                            .p(LogFieldConstants.EVENT, CenterEvent.SEND_MSG_TO_CHANNEL_EVENT);
                    if (future.isDone() && future.cause() != null) {
                        kvLogger.p(LogFieldConstants.ACTION, CenterEvent.Action.SEND_MSG_TO_CHANNEL_FAILED)
                                .p(LogFieldConstants.ERR_MSG, future.cause().getMessage())
                                .p(LogFieldConstants.Code, x00000503.getValue())
                                .p(LogFieldConstants.ReqData, request.toString())
                                .e(future.cause());
                        // 重发
                    } else if (future.isDone() && future.isSuccess()) {
                        kvLogger.p(LogFieldConstants.ACTION, CenterEvent.Action.SEND_MSG_TO_CHANNEL_SUCCESSFULLY)
                                .p(LogFieldConstants.Code, x00000000.getValue())
                                .i();
                        if (kvLogger.isDebug()) {
                            kvLogger.p(LogFieldConstants.ReqData, request.toString()).d();
                        }
                    }
                });
                return R.ok();
            }
        }
        return R.failed(x00000502.getValue(), x00000502.getMsg());
    }

    private Channel fetchChannelFromRemote(SendChannelReq request) {
        try {
            String relaySid = request.getServiceId() != null ? request.getServiceId() : request.getHostId();
            String remoteServer = String.valueOf(RedissonUtils.getLocalCache(
                    CenterApplicationRunner.consistentHash.getShard(relaySid).toString(),
                    relaySid
            ));

            String remoteUrl = "https://" + remoteServer + "/api/center/channel";

            // 获取远程接口


            return null;
        } catch (Exception e) {

            return null;
        }
    }


}