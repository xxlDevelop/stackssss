package org.yx.hoststack.center.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.Channel;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import org.yx.hoststack.center.apiservice.ApiServiceBase;
import org.yx.hoststack.center.common.config.channel.ChannelSendConfig;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.dto.ServiceDetailDTO;
import org.yx.hoststack.center.common.req.channel.SendChannelBasic;
import org.yx.hoststack.center.common.req.channel.SendChannelReq;
import org.yx.hoststack.center.common.utils.LocalHostUtil;
import org.yx.hoststack.center.retry.CenterMessageSender;
import org.yx.hoststack.center.service.biz.ServerCacheInfoServiceBiz;
import org.yx.hoststack.center.service.center.CenterService;
import org.yx.hoststack.center.ws.session.Session;
import org.yx.hoststack.center.ws.session.SessionAttrKeys;
import org.yx.hoststack.center.ws.session.SessionManager;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.lib.utils.constant.CommonConstants;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.StringUtil;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.yx.hoststack.center.common.enums.SysCode.*;
import static org.yx.lib.utils.token.YxTokenBuilderUtil.buildXUser;

@Service
@Slf4j
public class CenterServiceImpl implements CenterService {

    ServletRequest httpServletRequest;
    CenterMessageSender centerMessageSender;
    ApiServiceBase apiServiceBase;
    ChannelSendConfig channelSendConfig;
    ServerCacheInfoServiceBiz serverCacheInfoServiceBiz;
    LocalHostUtil localHostUtil;
    SessionManager sessionManager;

    public CenterServiceImpl(ServletRequest httpServletRequest,
                             CenterMessageSender centerMessageSender,
                             ApiServiceBase apiServiceBase,
                             ChannelSendConfig channelSendConfig,
                             LocalHostUtil localHostUtil,
                             SessionManager sessionManager) {
        this.httpServletRequest = httpServletRequest;
        this.centerMessageSender = centerMessageSender;
        this.apiServiceBase = apiServiceBase;
        this.channelSendConfig = channelSendConfig;
        this.localHostUtil = localHostUtil;
        this.sessionManager = sessionManager;
    }


    /**
     * Local lookup Channel
     */
    @Override
    public Channel findLocalChannel(SendChannelBasic request) {
        try {
            String id = StringUtil.isBlank(request.getHostId()) ? request.getServiceId() : request.getHostId();
//            if (StringUtil.isBlank(id)) {
//                return null;
//            }
//            if (Node.NODE_MAP.get(id) == null) {
//                return null;
//            }
//            return Node.NODE_MAP.get(id).getChannel();
            Optional<Session> sessionOpt = sessionManager.getSession(id);
            return sessionOpt.map(session -> session.getContext().channel()).orElse(null);
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
    public R<?> sendMsgToLocalChannel(SendChannelReq request) {
        String id = StringUtil.isBlank(request.getHostId()) ? request.getServiceId() : request.getHostId();
        Optional<Session> sessionOpt = sessionManager.getSession(id);
        if (sessionOpt.isPresent()) {
            try {
                sessionOpt.get().sendMsg(request.getTraceId(), CommonMessageWrapper.Body.parseFrom(request.getMsg()), null, null);
            } catch (InvalidProtocolBufferException e) {
                // TODO add syscode parse proto errorcode
                return R.failed(x00000504.getValue(), x00000504.getMsg());
            }
            return R.ok();
        } else {
            return R.failed(x00000502.getValue(), x00000502.getMsg());
        }
//        // 1. First look it up from the local centerNode
//        Optional<Channel> localChannel = Optional.ofNullable(findLocalChannel(request));
//        if (localChannel.isPresent()) {
//            Channel channel = localChannel.get();
//            if (channel.isActive()) {
//                ChannelFuture channelFuture = channel.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(request.getMsg())));
//                channelFuture.addListener(future -> {
//                    KvLogger kvLogger = KvLogger.instance(this)
//                            .p(LogFieldConstants.EVENT, CenterEvent.SEND_MSG_TO_CHANNEL_EVENT);
//                    if (future.isDone() && future.cause() != null) {
//                        kvLogger.p(LogFieldConstants.ACTION, CenterEvent.Action.SEND_MSG_TO_CHANNEL_FAILED)
//                                .p(LogFieldConstants.ERR_MSG, future.cause().getMessage())
//                                .p(LogFieldConstants.Alarm, 0)
//                                .p(LogFieldConstants.Code, x00000503.getValue())
//                                .p(LogFieldConstants.ReqData, request.toString())
//                                .e(future.cause());
//                        // Added to the resending Map
//                        String messageId = centerMessageSender.generateMessageId();
//                        centerMessageSender.reSendMap.putIfAbsent(messageId, new CenterMessageSender.ReSendMessage(channel, request, 0, messageId));
//                    } else if (future.isDone() && future.isSuccess()) {
//                        kvLogger.p(LogFieldConstants.ACTION, CenterEvent.Action.SEND_MSG_TO_CHANNEL_SUCCESSFULLY)
//                                .p(LogFieldConstants.Code, x00000000.getValue())
//                                .i();
//                        if (kvLogger.isDebug()) {
//                            kvLogger.p(LogFieldConstants.ReqData, request.toString()).d();
//                        }
//                    }
//                });
//                return R.ok();
//            }
//            return R.failed(x00000504.getValue(), x00000504.getMsg());
//        }
//        return R.failed(x00000502.getValue(), x00000502.getMsg());
    }

    @Override
    public R<?> sendMsgToLocalOrRemoteChannel(SendChannelReq request) {
        R<?> sendMsgToLocalChannel = sendMsgToLocalChannel(request);
        if (sendMsgToLocalChannel.getCode() == x00000502.getValue()) {
            // Remote send request
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.SEND_MSG_TO_LOCAL_OR_REMOTE_CHANNEL_EVENT)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.SEND_MSG_TO_LOCAL_OR_REMOTE_CHANNEL_REMOTE)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(request))
                    .i();
            return fetchChannelFromRemote(request);
        }
        return sendMsgToLocalChannel;
    }

    @Override
    public R<?> fetchChannelFromRemote(SendChannelReq sendChannelReq) {
        String remoteUrl = "";
        Map<String, String> requestHeaders = new HashMap<>();
        try {
            remoteUrl = buildRemoteUrl(sendChannelReq, channelSendConfig.getSendMsgUrl());
            requestHeaders = prepareRequestHeaders();

            String finalRemoteUrl = remoteUrl;
            String traceId = MDC.get(CommonConstants.TRACE_ID);
            if (StringUtil.isBlank(traceId)) {
                traceId = sendChannelReq.getTraceId();
            }
            return apiServiceBase.post(remoteUrl, traceId, requestHeaders, sendChannelReq)
                    .map(result -> JSON.parseObject(result, R.class))
                    .doOnError(e -> logError(e, finalRemoteUrl, sendChannelReq))
                    .onErrorReturn(R.failed(x00000506.getValue(), x00000506.getMsg()))
                    .doOnNext(r -> logSuccess(r, finalRemoteUrl, sendChannelReq)).block();
        } catch (Exception e) {
            KvLogger.instance(this)
                    .p(LogFieldConstants.EVENT, CenterEvent.FETCH_CHANNEL_FROM_REMOTE_EVENT)
                    .p(LogFieldConstants.ACTION, CenterEvent.Action.FETCH_CHANNEL_FROM_REMOTE_FAILED)
                    .p(LogFieldConstants.ERR_MSG, e.getMessage())
                    .p(LogFieldConstants.Alarm, 0)
                    .p(LogFieldConstants.API_URL, remoteUrl)
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(requestHeaders))
                    .p(LogFieldConstants.ReqData, JSON.toJSONString(sendChannelReq))
                    .p(LogFieldConstants.TRACE_ID, MDC.get(CommonConstants.TRACE_ID))
                    .e(e);
            return R.failed();
        }

    }

    @Override
    public String buildRemoteUrl(SendChannelBasic sendChannelBasic, String url) {
        if (StringUtil.isBlank(url)) {
            throw new RuntimeException("BuildRemoteUrl URL is null");
        }
        String id = StringUtil.isBlank(sendChannelBasic.getHostId()) ? sendChannelBasic.getServiceId() : sendChannelBasic.getHostId();
        if (StringUtil.isBlank(id)) {
            throw new RuntimeException("BuildRemoteUrl serviceId is null");
        }
        String remoteServer = sessionManager.getSessionAttr(id, SessionAttrKeys.REGISTER_IP_AND_PORT);
        if (remoteServer == null) {
            throw new RuntimeException("BuildRemoteUrl Remote server not found");
        }
        if (isLocal(remoteServer)) {
            throw new RuntimeException("BuildRemoteUrl Remote server is local");
        }
        return UriComponentsBuilder.fromUriString("http://" + remoteServer)
                .path(url)
                .build()
                .toUriString();
    }

    @Override
    public Map<String, String> prepareRequestHeaders() {
        Map<String, String> headers = new HashMap<>();
        // Add necessary headers here, possibly from MDC or other sources
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (attributes != null) {
            request = attributes.getRequest();
        }
        // Gets all header information for the current request
        if (request != null) {
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                headers.put(headerName, headerValue);
            }
        }
        // Add custom header information
        if (!headers.containsKey(CommonConstants.X_USER) || StringUtil.isBlank(headers.get(CommonConstants.X_USER))) {
            headers.put(CommonConstants.X_USER, MDC.get(CommonConstants.X_USER) != null ? MDC.get(CommonConstants.X_USER) : buildXUser(10000L, "Admin", 1L));
        }
        if (!headers.containsKey(CommonConstants.TRACE_ID_HEADER) || StringUtil.isBlank(headers.get(CommonConstants.TRACE_ID_HEADER))) {
            headers.put(CommonConstants.TRACE_ID_HEADER, MDC.get(CommonConstants.TRACE_ID));
        }
        if (!headers.containsKey(CommonConstants.TRACE_ID) || StringUtil.isBlank(headers.get(CommonConstants.TRACE_ID))) {
            headers.put(CommonConstants.TRACE_ID, MDC.get(CommonConstants.TRACE_ID));
        }

        return headers;
    }

    @Override
    public ServiceDetailDTO findNode(SendChannelBasic request) {
        return serverCacheInfoServiceBiz.getAgentInfo(request.hostId);
    }

    @Override
    public boolean isLocal(String hostIp) {
        if (hostIp == null) {
            throw new RuntimeException("HostIp is null");
        }
        if (hostIp.contains(":")) {
            hostIp = hostIp.split(":")[0];
        }
        return localHostUtil.isLocalAddress(hostIp);
    }

    private void logError(Throwable e, String remoteUrl, SendChannelReq sendChannelReq) {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, CenterEvent.FETCH_CHANNEL_FROM_REMOTE_EVENT)
                .p(LogFieldConstants.ACTION, CenterEvent.Action.FETCH_CHANNEL_FROM_REMOTE_FAILED)
                .p(LogFieldConstants.ERR_MSG, e.getMessage())
                .p(LogFieldConstants.Alarm, 0)
                .p(LogFieldConstants.API_URL, remoteUrl)
                .p(LogFieldConstants.ReqData, JSON.toJSONString(sendChannelReq))
                .p(LogFieldConstants.TRACE_ID, MDC.get(CommonConstants.TRACE_ID))
                .e(e);
    }

    private void logSuccess(R<?> r, String remoteUrl, SendChannelReq sendChannelReq) {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, CenterEvent.FETCH_CHANNEL_FROM_REMOTE_EVENT)
                .p(LogFieldConstants.ACTION, CenterEvent.Action.FETCH_CHANNEL_FROM_REMOTE_SUCCESS)
                .p(LogFieldConstants.API_URL, remoteUrl)
                .p(LogFieldConstants.ReqData, JSON.toJSONString(sendChannelReq))
                .p(LogFieldConstants.TRACE_ID, MDC.get(CommonConstants.TRACE_ID))
                .i();
    }


}