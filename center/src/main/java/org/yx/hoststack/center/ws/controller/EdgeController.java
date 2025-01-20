package org.yx.hoststack.center.ws.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.yx.hoststack.center.common.constant.CenterEvent;
import org.yx.hoststack.center.common.enums.RegisterNodeEnum;
import org.yx.hoststack.center.common.properties.ApplicationsProperties;
import org.yx.hoststack.center.common.redis.util.RedissonUtils;
import org.yx.hoststack.center.entity.IdcInfo;
import org.yx.hoststack.center.entity.RegionInfo;
import org.yx.hoststack.center.entity.RelayInfo;
import org.yx.hoststack.center.entity.ServiceDetail;
import org.yx.hoststack.center.service.IdcInfoService;
import org.yx.hoststack.center.service.RelayInfoService;
import org.yx.hoststack.center.service.ServiceDetailService;
import org.yx.hoststack.center.ws.common.Node;
import org.yx.hoststack.center.ws.controller.manager.CenterControllerManager;
import org.yx.hoststack.center.ws.heartbeat.HeartbeatMonitor;
import org.yx.hoststack.common.HostStackConstants;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.E2CMessage;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.SpringContextHolder;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.yx.hoststack.center.common.enums.RegisterNodeEnum.IDC;
import static org.yx.hoststack.center.common.enums.RegisterNodeEnum.RELAY;
import static org.yx.hoststack.center.common.enums.SysCode.*;
import static org.yx.hoststack.center.ws.CenterServer.*;
import static org.yx.hoststack.center.ws.common.Node.findNodeByServiceId;
import static org.yx.hoststack.center.ws.common.region.RegionInfoCache.getRegionByIp;


/**
 * Process Center Basic Message
 */
@Service
@RequiredArgsConstructor
public class EdgeController {
    {
        CenterControllerManager.add(ProtoMethodId.EdgeRegister, this::register);
        CenterControllerManager.add(ProtoMethodId.Ping, this::ping);
        CenterControllerManager.add(ProtoMethodId.IdcExit, this::idcExit);
    }

    public static final ConcurrentHashMap<String, IdcInfo> idcInfoCacheMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, RelayInfo> relayInfoCacheMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, ServiceDetail> serverDetailCacheMap = new ConcurrentHashMap<>();

    private final IdcInfoService idcInfoService;
    private final RelayInfoService relayInfoService;
    private final ServiceDetailService serviceDetailService;
    private final HeartbeatMonitor monitor;
    private final StringRedisTemplate redisTemplate;
    private final ApplicationsProperties applicationsProperties;

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
            final AtomicBoolean isErrorFlag = new AtomicBoolean(false);

            String serviceId = !StringUtils.isEmpty(header.getIdcSid()) ? header.getIdcSid() : header.getRelaySid();
            ctx.channel().attr(AttributeKey.valueOf("innerServiceId")).set(serviceId);
            String serviceIp = edgeRegister.getServiceIp();

            RegionInfo region = getRegionByIp(serviceIp);
            if (ObjectUtils.isEmpty(region)) {
                sendErrorResponse(ctx, message, x00000407.getValue(), x00000407.getMsg());
                return;
            }

            String nodeId = checkAndRegisterNode(ctx, header, message, serviceIp, region, isErrorFlag);

            KvLogger.instance(this).p(LogFieldConstants.EVENT, CenterEvent.CenterWsServer).p(LogFieldConstants.ACTION, CenterEvent.Action.CenterWsServer_EdgeRegisterCenter).p(LogFieldConstants.TRACE_ID, message.getHeader().getTraceId()).p(HostStackConstants.CHANNEL_ID, ctx.channel().id()).p("ServiceIp", serviceIp).p("Region", region).i();
            if (isErrorFlag.get()) {
                return;
            }

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
                                            .setId(nodeId)
                                            .setHbInterval(serverHbInterval)
                                            .build().toByteString()
                            ).build()).build();

            ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(returnMessage.toByteArray())));
        } catch (Exception ex) {
            sendErrorResponse(ctx, message, x00000500.getValue(), x00000500.getMsg());
        }
    }

    /**
     * idc exit
     *
     * @param ctx     ChannelHandlerContext
     * @param message CommonMessage
     */
    private void idcExit(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message) {
        try {
            ByteString payload = message.getBody().getPayload();
            E2CMessage.E2C_IdcExitReq exitReq = E2CMessage.E2C_IdcExitReq.parseFrom(payload);
            String idcSid = exitReq.getIdcSid();
            ctx.channel().attr(AttributeKey.valueOf("innerServiceId")).set(idcSid);

            ServiceDetail detail = serverDetailCacheMap.get(exitReq.getIdcSid());
            if (ObjectUtils.isEmpty(detail)) {
                sendErrorResponse(ctx, message, x00000406.getValue(), x00000406.getMsg());
                return;
            }

            offline(idcSid);

            CommonMessageWrapper.CommonMessage returnMessage = CommonMessageWrapper.CommonMessage.newBuilder()
                    .setBody(CommonMessageWrapper.Body.newBuilder().setMsg(x00000419.getMsg()).setCode(x00000419.getValue()).build())
                    .setHeader(CommonMessageWrapper.CommonMessage.newBuilder()
                            .getHeaderBuilder()
                            .setMethId(message.getHeader().getMethId())
                            .setLinkSide(CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE)
                            .setProtocolVer(CommonMessageWrapper.ENUM_PROTOCOL_VER.PROTOCOL_V1_VALUE)
                            .setIdcSid(message.getHeader().getIdcSid())
                            .setTraceId(message.getHeader().getTraceId())).build();
            ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(returnMessage.toByteArray())));
        } catch (Exception ex) {
            sendErrorResponse(ctx, message, x00000500.getValue(), x00000500.getMsg());
        }
    }

    /**
     * Edge ping
     *
     * @param ctx     ChannelHandlerContext
     * @param message CommonMessage
     */
    private void ping(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message) {
        try {
            RegisterNodeEnum type = ObjectUtils.isEmpty(message.getHeader().getIdcSid()) ? IDC : RELAY;
            String serviceId = ObjectUtils.isEmpty(message.getHeader().getIdcSid()) ? message.getHeader().getRelaySid() : message.getHeader().getIdcSid();

            serverDetailCacheMap.compute(serviceId, (key, existingDetail) -> {
                if (ObjectUtils.isEmpty(existingDetail)) {
                    sendErrorResponse(ctx, message, x00000408.getValue(), x00000408.getMsg());
                    return null;
                }
                existingDetail.setLastHbAt(new Date());
                monitor.updateHeartbeat(serviceId, type, expirationTime -> offline(serviceId));
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
     *check and register node
     * @author yijian
     * @date 2024/12/17 10:44
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public String checkAndRegisterNode(ChannelHandlerContext ctx, CommonMessageWrapper.Header header, CommonMessageWrapper.CommonMessage message, String serviceIp, RegionInfo region, AtomicBoolean isErrorFlag) {
        String serviceId = !StringUtils.isEmpty(header.getIdcSid()) ? header.getIdcSid() : header.getRelaySid();
        String relaySid = header.getRelaySid();
        Long serverDetailCount = redisTemplate.opsForValue().increment("serviceDetailCount", 1);
        String nodeId = region.getZoneCode() + "-" + region.getRegionCode() + "-" + (!StringUtils.isEmpty(header.getIdcSid()) ? IDC : RELAY) + "-" + String.format("%0" + applicationsProperties.getSequenceNumber() + "d", serverDetailCount);

        Node relayNode = null;
        if (!ObjectUtils.isEmpty(header.getRelaySid())) {
            saveRelayInfo(header.getRelaySid(), serviceIp, region, ctx, message, isErrorFlag, nodeId);
            RedissonUtils.setLocalCachedMap(serverConsistentHash.getShard(relaySid).toString(), relaySid, address + ":" + port);
            relayNode = findNodeByServiceId(relaySid);
        }

        if (!ObjectUtils.isEmpty(header.getIdcSid())) {
            String idcSid = header.getIdcSid();
            RedissonUtils.setLocalCachedMap(serverConsistentHash.getShard(idcSid).toString(), idcSid, address + ":" + port);
            saveIdcInfo(idcSid, region, serviceIp, relayNode, ctx, message, isErrorFlag, nodeId);
        }
        centerNode.printNodeInfoIterative();
        saveServerDetail(nodeId, serviceIp, header, serviceId, ctx, message, isErrorFlag);
        return nodeId;
    }


    private void saveRelayInfo(String serviceId, String serviceIp, RegionInfo region, ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message, AtomicBoolean isErrorFlag, String nodeId) {
        RelayInfo relayInfo = relayInfoCacheMap.computeIfAbsent(nodeId, key -> {
            CountDownLatch latch = new CountDownLatch(1);
            RelayInfo globalRelay = globalRelayInfoCacheMap.computeIfAbsent(nodeId, r -> {
                RelayInfo build = new RelayInfo().builder()
                        .zone((region.getZoneCode()))
                        .region(region.getRegionCode())
                        .relayIp(serviceIp)
                        .location(region.getLocation())
                        .relay(nodeId)
                        .build();
                CompletableFuture.supplyAsync(() -> relayInfoService.insert(build), virtualThreadExecutor).handle((result, throwable) -> {
                    try {
                        if (!ObjectUtils.isEmpty(throwable) || !result) {
                            handleSaveError(throwable, result, ctx, message, "SaveRelayInfo", x00000415.getMsg(), x00000415.getValue());
                            isErrorFlag.set(true);
                        }
                    } finally {
                        latch.countDown();
                    }
                    return result;
                });
                try {
                    latch.await(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    isErrorFlag.set(true);
                }

                return isErrorFlag.get() ? null : build;
            });

            return globalRelay;
        });
        centerNode.addOrUpdateNode(serviceId, RELAY, ctx.channel());

        int hashCode = Objects.hash(region.getZoneCode(), region.getRegionCode(), serviceId, serviceIp);
        if (hashCode != relayInfo.hashCode()) {
            CountDownLatch updateLatch = new CountDownLatch(1);
            relayInfo.setZone(region.getZoneCode());
            relayInfo.setRegion(region.getRegionCode());
            relayInfo.setRelayIp(serviceIp);
            RelayInfo finalRelayInfo = relayInfo;
            CompletableFuture.supplyAsync(() -> relayInfoService.update(finalRelayInfo), virtualThreadExecutor).handle((result, throwable) -> {
                try {
                    if (!ObjectUtils.isEmpty(throwable) || !result) {
                        handleSaveError(throwable, result, ctx, message, "UpdateRelayInfo", x00000416.getMsg(), x00000416.getValue());
                        isErrorFlag.set(true);
                        updateLatch.countDown();
                    }
                    updateLatch.await(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    isErrorFlag.set(true);
                }
                return isErrorFlag.get() ? null : result;
            });
        }
        relayInfoCacheMap.put(nodeId, relayInfo);
    }

    public void saveServerDetail(String nodeId, String serviceIp, CommonMessageWrapper.Header header, String serviceId, ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message, AtomicBoolean isErrorFlag) {
        RegisterNodeEnum type = !StringUtils.isEmpty(header.getIdcSid()) ? IDC : RELAY;
        ServiceDetail detail = serverDetailCacheMap.compute(serviceId, (key, existingDetail) -> {
            CountDownLatch latch = new CountDownLatch(1);
            if (ObjectUtils.isEmpty(existingDetail)) {
                existingDetail = globalServerDetailCacheMap.computeIfAbsent(serviceId, s -> ServiceDetail.builder()
                        .edgeId(nodeId)
                        .localIp(serviceIp)
                        .version("1.0")
                        .type(String.valueOf(type))
                        .serviceId(s)
                        .healthy(NumberUtils.BYTE_ONE)
                        .lastHbAt(new Date())
                        .build());
            }

            if (ObjectUtils.isEmpty(existingDetail.getId()) || NumberUtils.BYTE_ZERO.equals(existingDetail.getHealthy())) {
                ServiceDetail finalExistingDetail = existingDetail;
                existingDetail.setHealthy(NumberUtils.BYTE_ONE);
                existingDetail.setLastHbAt(new Date());
                existingDetail.setEdgeId(nodeId);
                existingDetail.setLocalIp(serviceIp);
                existingDetail.setType(String.valueOf(type));
                CompletableFuture.supplyAsync(() -> serviceDetailService.saveOrUpdate(finalExistingDetail), virtualThreadExecutor).handle((result, throwable) -> {
                    if (!ObjectUtils.isEmpty(throwable) || !result) {
                        try {
                            handleSaveError(throwable, result, ctx, message, "SaveOrUpdateServiceDetail", x00000417.getMsg(), x00000417.getValue());
                            isErrorFlag.set(true);
                        } finally {
                            latch.countDown();
                        }
                    }
                    return result;
                });
                try {
                    latch.await(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    isErrorFlag.set(true);
                }
            }

            return isErrorFlag.get() ? null : existingDetail;
        });
        if (!ObjectUtils.isEmpty(detail)) {
            SpringContextHolder.getBean(HeartbeatMonitor.class).updateHeartbeat(serviceId, type, expirationTime -> {
                EdgeController bean = SpringContextHolder.getBean(EdgeController.class);
                bean.offline(serviceId);
            });
        }
    }

    private void saveIdcInfo(String serviceId, RegionInfo region, String serviceIp, Node relayNode, ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message, AtomicBoolean isErrorFlag, String nodeId) {
        IdcInfo idcInfo = idcInfoCacheMap.computeIfAbsent(nodeId, key -> {
            CountDownLatch latch = new CountDownLatch(1);
            IdcInfo globalIdcInfo = globalIdcInfoCacheMap.computeIfAbsent(nodeId, i -> {
                IdcInfo build = new IdcInfo().builder()
                        .zone((region.getZoneCode()))
                        .region(region.getRegionCode())
                        .idcIp(serviceIp)
                        .location(region.getLocation())
                        .createAt(new Date())
                        .lastUpdateAt(new Date())
                        .idc(nodeId).build();
                CompletableFuture.supplyAsync(() -> idcInfoService.save(build), virtualThreadExecutor).handle((result, throwable) -> {
                    try {
                        if (!ObjectUtils.isEmpty(throwable) || !result) {
                            handleSaveError(throwable, result, ctx, message, "SaveIdcInfo", x00000413.getMsg(), x00000413.getValue());
                            isErrorFlag.set(true);
                        }
                    } finally {
                        latch.countDown();
                    }
                    return result;
                });
                try {
                    latch.await(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    isErrorFlag.set(true);
                }
                return isErrorFlag.get() ? null : build;
            });
            return globalIdcInfo;
        });

        int hashCode = Objects.hash(region.getZoneCode(), region.getRegionCode(), serviceId, serviceIp);
        if (hashCode != idcInfo.hashCode()) {
            CountDownLatch updateCountLatch = new CountDownLatch(1);
            idcInfo.setZone(region.getZoneCode());
            idcInfo.setRegion(region.getRegionCode());
            idcInfo.setIdcIp(serviceIp);
            idcInfo.setLastUpdateAt(new Date());
            CompletableFuture.supplyAsync(() -> idcInfoService.updateById(idcInfo), virtualThreadExecutor).handle((result, throwable) -> {
                try {
                    if (!ObjectUtils.isEmpty(throwable) || !result) {
                        handleSaveError(throwable, result, ctx, message, "UpdateIdcInfo", x00000414.getMsg(), x00000414.getValue());
                        isErrorFlag.set(true);
                        updateCountLatch.countDown();
                    }
                    updateCountLatch.await(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    isErrorFlag.set(true);
                }

                return isErrorFlag.get() ? null : result;
            });
        }

        idcInfoCacheMap.put(nodeId, idcInfo);
        if (!ObjectUtils.isEmpty(relayNode)) {
            relayNode.addOrUpdateNode(serviceId, IDC, ctx.channel(), relayNode);
        } else {
            centerNode.addOrUpdateNode(serviceId, IDC, ctx.channel());
        }
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
     *
     * timeout offline
     * @author yijian
     * @date 2025/1/3 17:43
     */
    public void offline(String serviceId) {
        CompletableFuture.runAsync(() -> {
            try {
                Node node = findNodeByServiceId(serviceId);
                if (node == null) {
                    return;
                }
                List<String> serviceIds = processChildrenHealth(node);

                processNodeHealth(node);

                updateServiceDetails(serviceId, serviceIds);

                centerNode.printNodeInfoIterative();
                node.getChannel().disconnect();
            } catch (Exception e) {
                KvLogger.instance(SpringContextHolder.getBean(EdgeController.class))
                        .p(LogFieldConstants.ACTION, "OfflineError")
                        .p(LogFieldConstants.ERR_MSG, e.getMessage())
                        .p("params", serviceId)
                        .e();
            }
        }, virtualThreadExecutor);
    }

    private List<String> processChildrenHealth(Node node) {
        return node.getChildren().parallelStream()
                .peek(child -> updateNodeHealthAndCache(child))
                .map(Node::getServiceId)
                .collect(Collectors.toList());
    }

    private void processNodeHealth(Node node) {
        updateNodeHealthAndCache(node);
        node.removeNodeRecursively(node);
    }

    private void updateNodeHealthAndCache(Node node) {
        ServiceDetail detail = serverDetailCacheMap.get(node.getServiceId());
        if (!ObjectUtils.isEmpty(detail)) {
            detail.setHealthy(NumberUtils.BYTE_ZERO);
            serverDetailCacheMap.put(node.getServiceId(), detail);
            RedissonUtils.delLocalCachedMap(serverConsistentHash.getShard(node.getServiceId()).toString(), node.getServiceId());
        }
    }

    private void updateServiceDetails(String serviceId, List<String> serviceIds) {
        ServiceDetailService service = SpringContextHolder.getBean(ServiceDetailService.class);
        serviceIds.add(serviceId);
        if (!CollectionUtils.isEmpty(serviceIds)) {
            boolean update = service.update(new LambdaUpdateWrapper<ServiceDetail>()
                    .set(ServiceDetail::getHealthy, NumberUtils.INTEGER_ZERO)
                    .in(ServiceDetail::getServiceId, serviceIds));
            if (!update) {
                KvLogger.instance(SpringContextHolder.getBean(EdgeController.class))
                        .p(LogFieldConstants.ACTION, "UpdateServiceDetailError")
                        .p(LogFieldConstants.ERR_MSG, "Update Service Detail health error")
                        .p("params", serviceIds)
                        .e();
            }
        }
    }

    public void handleSaveError(Throwable throwable, Boolean result, ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message, String action, String msg, Integer code) {
        KvLogger.instance(this)
                .p(LogFieldConstants.ACTION, action)
                .p(LogFieldConstants.ERR_MSG, !ObjectUtils.isEmpty(throwable) ? throwable.getMessage() : "customerThrowException")
                .p("result", result)
                .e();
        sendErrorResponse(ctx, message, code, msg);
    }
}
