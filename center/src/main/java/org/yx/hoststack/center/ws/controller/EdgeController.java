package org.yx.hoststack.center.ws.controller;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.yx.hoststack.center.CenterApplicationRunner;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.enums.RegisterNodeEnum;
import org.yx.hoststack.center.entity.IdcInfo;
import org.yx.hoststack.center.entity.RegionInfo;
import org.yx.hoststack.center.entity.RelayInfo;
import org.yx.hoststack.center.entity.ServiceDetail;
import org.yx.hoststack.center.service.IdcInfoService;
import org.yx.hoststack.center.service.RelayInfoService;
import org.yx.hoststack.center.service.ServiceDetailService;
import org.yx.hoststack.center.ws.CenterServer;
import org.yx.hoststack.center.ws.common.Node;
import org.yx.hoststack.center.ws.controller.manager.CenterControllerManager;
import org.yx.hoststack.center.ws.task.HeartbeatMonitor;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.E2CMessage;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.SpringContextHolder;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import static org.yx.hoststack.center.CenterApplicationRunner.monitor;
import static org.yx.hoststack.center.common.enums.RegisterNodeEnum.IDC;
import static org.yx.hoststack.center.common.enums.RegisterNodeEnum.RELAY;
import static org.yx.hoststack.center.common.enums.SysCode.*;
import static org.yx.hoststack.center.ws.CenterServer.*;
import static org.yx.hoststack.center.ws.common.Node.findNodeByServiceId;


/**
 * Process Center Basic Message
 */
@Service
@RequiredArgsConstructor
public class EdgeController {
    {
        CenterControllerManager.add(ProtoMethodId.EdgeRegister, this::register);
        CenterControllerManager.add(ProtoMethodId.Ping, this::ping);
    }

    public static final ConcurrentHashMap<String, IdcInfo> idcInfoCacheMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, RelayInfo> relayInfoCacheMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, ServiceDetail> serverDetailCacheMap = new ConcurrentHashMap<>();

    private final IdcInfoService idcInfoService;
    private final RelayInfoService relayInfoService;
    private final ServiceDetailService serviceDetailService;

    @Qualifier("virtualThreadExecutor")
    private final Executor virtualThreadExecutor;
    @Value("${applications.serverHbInterval}")
    private Integer serverHbInterval;

    /**
     * Edge Register Center Success Result
     *
     * @param ctx     ChannelHandlerContext
     * @param message CommonMessage
     */
    private void register(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message) {
        try {
            ByteString payload = message.getBody().getPayload();
            CommonMessageWrapper.Header header = message.getHeader();
            E2CMessage.E2C_EdgeRegisterReq edgeRegister = E2CMessage.E2C_EdgeRegisterReq.parseFrom(payload);
            String serviceId = !StringUtils.isEmpty(header.getIdcSid()) ? header.getIdcSid() : header.getRelaySid();
            ctx.channel().attr(AttributeKey.valueOf("innerServiceId")).set(serviceId);
            String serviceIp = edgeRegister.getServiceIp();

            RegionInfo region = getRegionByIp(serviceIp);
            if (ObjectUtils.isEmpty(region)) {
                sendErrorResponse(ctx, message, x00000407.getValue(), x00000407.getMsg());
                return;
            }

            Long nodeId = checkAndRegisterNode(ctx, header, serviceIp, region);

            KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CenterWsServer).p(LogFieldConstants.ACTION, CenterEvent.Action.CenterWsServer_EdgeRegisterCenter).p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId()).p(HostStackConstants.CHANNEL_ID, ctx.channel().id()).p("ServiceIp", serviceIp).p("Region", region).i();

            CommonMessageWrapper.CommonMessage returnMessage = CommonMessageWrapper.CommonMessage.newBuilder()
                    .setHeader(CommonMessageWrapper.CommonMessage.newBuilder()
                            .getHeaderBuilder()
                            .setMethId(message.getHeader().getMethId())
                            .setLinkSide(CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE)
                            .setProtocolVer(CommonMessageWrapper.ENUM_PROTOCOL_VER.PROTOCOL_V1_VALUE)
                            .setZone(region.getZoneCode())
                            .setRegion(region.getRegionCode())
                            .setIdcSid(message.getHeader().getIdcSid())
                            .setRelaySid(message.getHeader().getRelaySid())
                            .setMethId(message.getHeader().getMethId())
                            .setTraceId(message.getHeader().getTraceId()))
                    .setBody(CommonMessageWrapper.Body.newBuilder().setCode(0)
                            .setPayload(
                                    C2EMessage.C2E_EdgeRegisterResp.newBuilder()
                                            .setId(String.valueOf(nodeId))
                                            .setHbInterval(serverHbInterval)
                                            .build().toByteString()
                            ).build()).build();

            ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(returnMessage.toByteArray())));
        } catch (Exception ex) {
            sendErrorResponse(ctx, message, x00000500.getValue(), x00000500.getMsg());
        }
    }

    /**
     * Get Region By Ip
     *
     * @author yijian
     * @date 2024/12/16 15:50
     */
    private RegionInfo getRegionByIp(String serviceIp) {
        List<RegionInfo> regionInfos = globalRegionInfoCacheMap.get(CenterServer.REGION_CACHE_KEY);
        if (!CollectionUtils.isEmpty(regionInfos)) {
            Optional<RegionInfo> first = regionInfos.parallelStream().filter(x -> x.getPublicIpList().contains(serviceIp)).findFirst();
            if (first.isPresent()) {
                return first.get();
            }
        }
        return null;
    }

    /**
     *
     *check and register node
     * @author yijian
     * @date 2024/12/17 10:44
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Long checkAndRegisterNode(ChannelHandlerContext ctx, CommonMessageWrapper.Header header, String serviceIp, RegionInfo region) {
        String serviceId = !StringUtils.isEmpty(header.getIdcSid()) ? header.getIdcSid() : header.getRelaySid();
        Node relayNode = null;
        if (!ObjectUtils.isEmpty(header.getRelaySid())) {
            RelayInfo relayInfo = relayInfoCacheMap.getOrDefault(header.getRelaySid(), new RelayInfo());
            if (ObjectUtils.isEmpty(relayInfo.getId()) && !relayInfoCacheMap.containsKey(header.getRelaySid())) {
                relayInfo = relayInfoCacheMap.computeIfAbsent(serviceId, key -> {
                    RelayInfo globalRelay = globalRelayInfoCacheMap.computeIfAbsent(serviceId, r -> {
                        RelayInfo build = new RelayInfo().builder()
                                .zone((region.getZoneCode()))
                                .region(region.getRegionCode())
                                .relayIp(serviceIp)
                                .location(region.getLocation())
                                .relay(r)
                                .build();

                        CompletableFuture.runAsync(() -> {
                            try {
                                relayInfoService.insert(build);
                            } catch (Exception e) {
                                KvLogger.instance(this).p(LogFieldConstants.ACTION, "SaveRelayInfo").p(LogFieldConstants.ERR_MSG, e.getMessage()).e();
                            }
                        }, virtualThreadExecutor);
                        return build;
                    });

                    return globalRelay;
                });
                centerNode.addOrUpdateNode(header.getRelaySid(), RELAY, ctx.channel());
            }

            int hashCode = Objects.hash(region.getZoneCode(), region.getRegionCode(), serviceId, serviceIp);
            if (hashCode != relayInfo.hashCode()) {
                relayInfo.setZone(region.getZoneCode());
                relayInfo.setRegion(region.getRegionCode());
                relayInfo.setRelayIp(serviceIp);
                RelayInfo finalRelayInfo = relayInfo;
                CompletableFuture.runAsync(() -> {
                    try {
                        relayInfoService.update(finalRelayInfo);
                    } catch (Exception e) {
                        KvLogger.instance(this).p(LogFieldConstants.ACTION, "UpdateRelayInfo").p(LogFieldConstants.ERR_MSG, e.getMessage()).e();
                    }
                }, virtualThreadExecutor);
            }

            relayInfoCacheMap.put(header.getRelaySid(), relayInfo);
            relayNode = findNodeByServiceId(header.getRelaySid());
        }


        if (!ObjectUtils.isEmpty(header.getIdcSid())) {
            IdcInfo idcInfo = idcInfoCacheMap.computeIfAbsent(serviceId, key -> {
                IdcInfo globalIdcInfo = globalIdcInfoCacheMap.computeIfAbsent(serviceId, i -> {
                    IdcInfo build = new IdcInfo().builder()
                            .zone((region.getZoneCode()))
                            .region(region.getRegionCode())
                            .idcIp(serviceIp)
                            .location(region.getLocation())
                            .createAt(new Date())
                            .lastUpdateAt(new Date())
                            .idc(i).build();
                    CompletableFuture.runAsync(() -> {
                        try {
                            idcInfoService.save(build);
                        } catch (Exception e) {
                            KvLogger.instance(this).p(LogFieldConstants.ACTION, "SaveIdcInfo").p(LogFieldConstants.ERR_MSG, e.getMessage()).e();
                        }
                    }, virtualThreadExecutor);
                    return build;
                });

                return globalIdcInfo;
            });

            int hashCode = Objects.hash(region.getZoneCode(), region.getRegionCode(), serviceId, serviceIp);
            if (hashCode != idcInfo.hashCode()) {
                idcInfo.setZone(region.getZoneCode());
                idcInfo.setRegion(region.getRegionCode());
                idcInfo.setIdcIp(serviceIp);
                idcInfo.setLastUpdateAt(new Date());
                CompletableFuture.runAsync(() -> {
                    try {
                        idcInfoService.updateById(idcInfo);
                    } catch (Exception e) {
                        KvLogger.instance(this).p(LogFieldConstants.ACTION, "UpdateIdcInfo").p(LogFieldConstants.ERR_MSG, e.getMessage()).e();
                    }
                }, virtualThreadExecutor);
            }

            idcInfoCacheMap.put(header.getIdcSid(), idcInfo);
            if (!ObjectUtils.isEmpty(relayNode)) {
                relayNode.addOrUpdateNode(header.getIdcSid(), IDC, ctx.channel(), relayNode);
            } else {
                centerNode.addOrUpdateNode(header.getIdcSid(), IDC, ctx.channel());
            }
        }
        centerNode.printNodeInfo(2);

        Long nodeId = Math.abs(new BigInteger(1, DigestUtil.md5Hex(region.getZoneCode() + region.getRegionCode() + (!StringUtils.isEmpty(header.getIdcSid()) ? IDC : RELAY) + serviceIp + serviceId).getBytes()).longValue());
        updateServerDetail(nodeId, serviceIp, header, serviceId);

        return nodeId;
    }

    public void updateServerDetail(Long nodeId, String serviceIp, CommonMessageWrapper.Header header, String serviceId) {
        RegisterNodeEnum type = !StringUtils.isEmpty(header.getIdcSid()) ? IDC : RELAY;
        ServiceDetail serviceDetail = serverDetailCacheMap.computeIfAbsent(serviceId, key -> {
            ServiceDetail globalServiceDetail = globalServerDetailCacheMap.computeIfAbsent(serviceId, s -> {
                ServiceDetail build = ServiceDetail.builder()
                        .edgeId(nodeId)
                        .localIp(serviceIp)
                        .version("1.0")
                        .type(String.valueOf(type))
                        .serviceId(s)
                        .healthy(NumberUtils.BYTE_ONE)
                        .lastHbAt(new Date())
                        .build();

                CompletableFuture.runAsync(() -> {
                    try {
                        serviceDetailService.insert(build);
                    } catch (Exception e) {
                        KvLogger.instance(this).p(LogFieldConstants.ACTION, "SaveServiceDetail").p(LogFieldConstants.ERR_MSG, e.getMessage()).e();
                    }
                }, virtualThreadExecutor);
                return build;
            });
            return globalServiceDetail;
        });
        SpringContextHolder.getBean(HeartbeatMonitor.class).updateHeartbeat(serviceId, type, expirationTime -> {
            EdgeController bean = SpringContextHolder.getBean(EdgeController.class);
            bean.offline(serviceId, serviceDetail.getId(), type, expirationTime);
        });
        serverDetailCacheMap.put(serviceId, serviceDetail);
    }

    /**
     *
     * send error response
     * @author yijian

     * @date 2024/12/17 10:44
     */
    private void sendErrorResponse(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message, int code, String msg, String... args) {
        List<String> params = List.of(args);
        CommonMessageWrapper.CommonMessage errorMessage = CommonMessageWrapper.CommonMessage.newBuilder()
                .setHeader(CommonMessageWrapper.CommonMessage.newBuilder().getHeaderBuilder()
                        .setMethId(CollectionUtils.isEmpty(params) ? message.getHeader().getMethId() : Integer.parseInt(params.get(0)))
                        .setLinkSide(CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE)
                        .setProtocolVer(CommonMessageWrapper.ENUM_PROTOCOL_VER.PROTOCOL_V1_VALUE)
                        .setZone(message.getHeader().getZone())
                        .setRegion(message.getHeader().getRegion())
                        .setIdcSid(message.getHeader().getIdcSid())
                        .setRelaySid(message.getHeader().getRelaySid())
                        .setTraceId(message.getHeader().getTraceId()))
                .setBody(CommonMessageWrapper.Body.newBuilder().setCode(code).setMsg(msg).build()).build();
        ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(errorMessage.toByteArray())));
    }


    /**
     * Edge ping
     *
     * @param ctx     ChannelHandlerContext
     * @param message CommonMessage
     */
    private void ping(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message) {
        try {
            HeartbeatMonitor monitor = new HeartbeatMonitor();
            RegisterNodeEnum type = ObjectUtils.isEmpty(message.getHeader().getIdcSid()) ? IDC : RELAY;
            String serviceId = ObjectUtils.isEmpty(message.getHeader().getIdcSid()) ? message.getHeader().getRelaySid() : message.getHeader().getIdcSid();

            serverDetailCacheMap.compute(serviceId, (key, existingDetail) -> {
                if (ObjectUtils.isEmpty(existingDetail)) {
                    sendErrorResponse(ctx, message, x00000408.getValue(), x00000408.getMsg());
                    return null;
                }
                existingDetail.setLastHbAt(new Date());
                monitor.updateHeartbeat(serviceId, type, expirationTime -> offline(serviceId, existingDetail.getId(), type, expirationTime));
                return existingDetail;
            });
            CommonMessageWrapper.CommonMessage returnMessage = CommonMessageWrapper.CommonMessage.newBuilder()
                    .setHeader(CommonMessageWrapper.CommonMessage.newBuilder().getHeaderBuilder()
                            .setMethId(ProtoMethodId.Pong.getValue())
                            .setLinkSide(CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE)
                            .setProtocolVer(CommonMessageWrapper.ENUM_PROTOCOL_VER.PROTOCOL_V1_VALUE)
                            .setZone(message.getHeader().getZone())
                            .setRegion(message.getHeader().getRegion())
                            .setIdcSid(message.getHeader().getIdcSid())
                            .setRelaySid(message.getHeader().getRelaySid())
                            .setTraceId(message.getHeader().getTraceId()))

                    .setBody(CommonMessageWrapper.Body.newBuilder().setCode(x00000000.getValue()).setMsg(x00000000.getMsg()).build())
                    .build();
            byte[] protobufMessage = returnMessage.toByteArray();
            ByteBuf byteBuf = Unpooled.wrappedBuffer(protobufMessage);
            ctx.writeAndFlush(new BinaryWebSocketFrame(byteBuf));

        } catch (Exception ex) {
            sendErrorResponse(ctx, message, x00000500.getValue(), x00000500.getMsg());
        }
    }

    /**
     *
     * timeout offline
     * @author yijian
     * @date 2025/1/3 17:43
     */
    public void offline(String serviceId, Long id, RegisterNodeEnum type, long expirationTime) {
        ServiceDetailService service = SpringContextHolder.getBean(ServiceDetailService.class);
        ServiceDetail detail = serverDetailCacheMap.get(serviceId);
        CompletableFuture.runAsync(() -> {
            try {

                Node node = findNodeByServiceId(serviceId);
                if (node == null || !ObjectUtils.isEmpty(detail)) {
                    long time = detail.getLastHbAt().getTime();
                    if (time > expirationTime) return;
                }

                detail.setHealthy(NumberUtils.BYTE_ZERO);
                serverDetailCacheMap.put(serviceId, detail);

                Channel channel = node.getChannel();
                node.removeNodeRecursively(node);
                service.update(new LambdaUpdateWrapper<ServiceDetail>().set(ServiceDetail::getHealthy, NumberUtils.INTEGER_ZERO).eq(ServiceDetail::getId, id));

                CenterServer.centerNode.printNodeInfo(2);
                channel.close();
            } catch (Exception e) {
                KvLogger.instance(SpringContextHolder.getBean(EdgeController.class)).p(LogFieldConstants.ACTION, "OfflineError").p(LogFieldConstants.ERR_MSG, e.getMessage()).e();
            }
        }, virtualThreadExecutor);

        KvLogger.instance(SpringContextHolder.getBean(EdgeController.class)).p(LogFieldConstants.ACTION, String.format("%s:ServiceID:%s-HeatBeat Timeout", type, serviceId))
                .p("expirationTime", expirationTime)
                .p("currentTime", detail.getLastHbAt())
                .i();
    }
}
