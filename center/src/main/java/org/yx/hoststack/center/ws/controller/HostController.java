package org.yx.hoststack.center.ws.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.yx.hoststack.center.apiservice.uc.UserCenterTenantInfoApiService;
import org.yx.hoststack.center.apiservice.uc.resp.TenantInfoResp;
import org.yx.hoststack.center.common.enums.AgentTypeEnum;
import org.yx.hoststack.center.common.enums.RegisterNodeEnum;
import org.yx.hoststack.center.common.redis.util.RedissonUtils;
import org.yx.hoststack.center.entity.*;
import org.yx.hoststack.center.jobs.HostJob;
import org.yx.hoststack.center.service.*;
import org.yx.hoststack.center.ws.common.Node;
import org.yx.hoststack.center.ws.controller.manager.CenterControllerManager;
import org.yx.hoststack.center.ws.heartbeat.HeartbeatMonitor;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.E2CMessage;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.SpringContextHolder;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.yx.hoststack.center.common.enums.SysCode.*;
import static org.yx.hoststack.center.ws.CenterServer.*;
import static org.yx.hoststack.center.ws.common.Node.findNodeByServiceId;
import static org.yx.hoststack.center.ws.common.region.RegionInfoCache.getRegionByZoneRegionCode;


/**
 * Process Center Basic Message
 */
@Service("WsHostController")
@RequiredArgsConstructor
public class HostController {
    private final HostJob host;

    {
        CenterControllerManager.add(ProtoMethodId.HostInitialize, this::init);
        CenterControllerManager.add(ProtoMethodId.HostHeartbeat, this::ping);
        CenterControllerManager.add(ProtoMethodId.HostExit, this::hostExit);
    }

    public static final ConcurrentHashMap<String, Host> hostCurrentMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, Container> containerCurrentMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, SysModule> sysModuleCacheMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, List<AgentGpu>> gpuInfosCacheMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, AgentCpu> cpuInfosCacheMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, AgentSession> agentSessionCacheMap = new ConcurrentHashMap<>();

    private final HeartbeatMonitor monitor;
    private final HostService hostService;
    private final AgentGpuService agentGpuService;
    private final AgentCpuService agentCpuService;
    private final SysModuleService sysModuleService;
    private final ContainerService containerService;
    private final AgentSessionService agentSessionService;
    private final UserCenterTenantInfoApiService userCenterTenantInfoApiService;
    private final StringRedisTemplate redisTemplate;

    @Qualifier("virtualThreadExecutor")
    private final Executor virtualThreadExecutor;
    @Value("${applications.serverHbInterval}")
    private Integer serverHbInterval;

    /**
     * host initialize
     *
     * @param ctx     ChannelHandlerContext
     * @param message CommonMessage
     */
    private void init(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message) {
        ByteString payload = message.getBody().getPayload();
        E2CMessage.E2C_HostInitializeReq agentInit = null;
        try {
            agentInit = E2CMessage.E2C_HostInitializeReq.parseFrom(payload);
            CommonMessageWrapper.Header header = message.getHeader();
            boolean updateFlag = false;
            AtomicReference<TenantInfoResp> tenantInfo = new AtomicReference<>(null);
            String serviceId = !StringUtils.isEmpty(header.getIdcSid()) ? header.getIdcSid() : header.getRelaySid();
            Node parentNode = findNodeByServiceId(serviceId);
            if (ObjectUtils.isEmpty(parentNode)) {
                sendHostInitErrorResponse(ctx, agentInit.getDevSn(), message, x00000601.getValue(), x00000601.getMsg());
                return;
            }
//
//            Mono<TenantInfoResp> mono = userCenterTenantInfoApiService.checkAuthToken(agentInit.getXToken());
//            mono.subscribe(result -> tenantInfo.set(result), error -> sendErrorResponse(ctx, message, x00000409.getValue(), x00000409.getMsg()));

            String containerShardKey = hostConsistentHash.getShard(agentInit.getDevSn()).toString();
            Container container = RedissonUtils.getLocalCache(containerShardKey, agentInit.getDevSn());
            if (!agentInit.getAgentType().equals(AgentTypeEnum.HOST.getName()) && !ObjectUtils.isEmpty(agentInit.getHostId()) && ObjectUtils.isEmpty(container)) {
                sendHostInitErrorResponse(ctx, agentInit.getDevSn(), message, x00000600.getValue(), x00000600.getMsg());
                return;
            }

            RegionInfo region = getRegionByZoneRegionCode(header.getZone(), header.getRegion());
            if (ObjectUtils.isEmpty(region)) {
                sendHostInitErrorResponse(ctx, agentInit.getDevSn(), message, x00000410.getValue(), x00000410.getMsg());
                return;
            }
            AgentTypeEnum agentType = AgentTypeEnum.fromString(agentInit.getAgentType());
            String agentId = StringUtils.EMPTY;
            CompletableFuture<Boolean> future = CompletableFuture.completedFuture(true);
            ;
            List<JSONObject> netList = new ArrayList<>(agentInit.getNetCardListList().size());
            agentInit.getNetCardListList().forEach(netCardInfo -> {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("netCardName", netCardInfo.getNetCardName());
                jsonObject.put("netCardType", netCardInfo.getNetCardType());
                jsonObject.put("netCardLinkSpeed", netCardInfo.getNetCardLinkSpeed());
                netList.add(jsonObject);
            });
            switch (agentType) {
                case HOST:
                    Host host = hostCurrentMap.get(agentInit.getDevSn());
                    if (ObjectUtils.isEmpty(host)) {
                        String shardKey = hostConsistentHash.getShard(agentInit.getDevSn()).toString();
                        host = RedissonUtils.getLocalCache(shardKey, agentInit.getDevSn());

                        if (!ObjectUtils.isEmpty(host)) {
                            updateFlag = validateRegion(header, host.getZone(), host.getRegion(), updateFlag, agentId, agentInit.getLocalIp(), agentInit.getDevSn());
                        }
                        if (updateFlag || ObjectUtils.isEmpty(host)) {
                            host = Host.builder().hostId(updateFlag ? host.getHostId() : convertToHex(region.getRegionId()) + DigestUtils.md5Hex(agentInit.getDevSn()))
                                    .agentVersion(agentInit.getAgentVersion())
                                    .agentType(agentType.getName())
                                    .startTime(new Date(agentInit.getOsStartTs()))
                                    .devSn(agentInit.getDevSn())
                                    .osType(agentInit.getOsType())
                                    .osVersion(agentInit.getOsVersion())
                                    .osMem(String.valueOf(agentInit.getOsMem()))
                                    .resourcePool(agentInit.getResourcePool())
                                    .runtimeEnv(agentInit.getRuntimeEnv())
                                    .diskInfo(agentInit.getDisk())
                                    .networkCardInfo(JSONObject.toJSONString(netList))
                                    .zone(message.getHeader().getZone())
                                    .region(message.getHeader().getRegion())
                                    .idc(message.getHeader().getIdcSid())
                                    .hostIp(agentInit.getLocalIp())
                                    .gpuNum(agentInit.getGpuListCount())
                                    .cpuNum(agentInit.getCpuSpec().getCpuNum())
                                    .baremetalProvider(String.valueOf(message.getHeader().getTenantId()))
                                    .detailedId(agentInit.getDetailedId())
                                    .proxy(agentInit.getProxy())
                                    .lastHbAt(new Date())
//                                .ak(tenantInfo.getAk())
//                                .sk(tenantInfo.getSk())
                                    .build();
                            agentId = host.getHostId();
                            final boolean finalUpdateFlag = updateFlag;
                            Host finalHost = host;
                            future = CompletableFuture.supplyAsync(() -> {
                                boolean result = hostService.saveOrUpdate(finalHost);
                                if (result) {
                                    hostCurrentMap.put(finalHost.getDevSn(), finalHost);
                                    RedissonUtils.setLocalCachedMap(shardKey, finalHost.getDevSn(), finalHost);
                                }
                                return result;
                            }, virtualThreadExecutor);
                        } else {
                            hostCurrentMap.put(agentInit.getDevSn(), host);
                            agentId = host.getHostId();
                        }
                    } else {
                        agentId = host.getHostId();
                    }
                    break;
                case CONTAINER, BENCHMARK:
                    if (!ObjectUtils.isEmpty(container)) {
                        updateFlag = validateRegion(header, container.getZone(), container.getRegion(), updateFlag, agentId, agentInit.getLocalIp(), agentInit.getDevSn());
                    }
                    if (!updateFlag && !ObjectUtils.isEmpty(container)) {
                        agentId = container.getContainerId();
                        containerCurrentMap.put(agentInit.getDevSn(), container);
                    } else {
                        String uniqueId = convertToHex(region.getRegionId()) + DigestUtils.md5Hex(agentInit.getDevSn());
                        Long sequenceNumber = redisTemplate.opsForValue().increment("sequenceNumber:uniqueId:" + uniqueId, 1);
                        container = Container.builder()
                                .containerId(updateFlag ? container.getContainerId() : uniqueId + "#" + sequenceNumber)
                                .hostId(uniqueId)
                                .sequenceNumber(Integer.valueOf(sequenceNumber.toString()))
                                .agentVersion(agentInit.getAgentVersion())
                                .agentType(agentType.getName())
                                .startTime(new Date(agentInit.getOsStartTs()))
                                .devSn(agentInit.getDevSn())
                                .osType(agentInit.getOsType())
                                .osVersion(agentInit.getOsVersion())
                                .osMem(String.valueOf(agentInit.getOsMem()))
                                .resourcePool(agentInit.getResourcePool())
                                .runtimeEnv(agentInit.getRuntimeEnv())
                                .diskInfo(agentInit.getDisk())
                                .networkCardInfo(JSONObject.toJSONString(netList))
                                .zone(message.getHeader().getZone())
                                .region(message.getHeader().getRegion())
                                .idc(message.getHeader().getIdcSid())
                                .containerIp(agentInit.getLocalIp())
                                .gpuNum(agentInit.getGpuListCount())
                                .cpuNum(agentInit.getCpuSpec().getCpuNum())
                                .containerProvider(String.valueOf(message.getHeader().getTenantId()))
                                .detailedId(agentInit.getDetailedId())
                                .proxy(agentInit.getProxy())
                                .lastHbAt(new Date())
//                                .ak(tenantInfo.getAk())
//                                .sk(tenantInfo.getSk())
                                .build();
                        AtomicReference<Container> atomicReference = new AtomicReference<>(container);
                        future = CompletableFuture.supplyAsync(() -> {
                            boolean result;
                            result = containerService.saveOrUpdate(atomicReference.get());
                            if (result) {
                                containerCurrentMap.put(atomicReference.get().getDevSn(), atomicReference.get());
                                RedissonUtils.setLocalCachedMap(containerShardKey, atomicReference.get().getDevSn(), atomicReference.get());
                            }
                            return result;
                        }, virtualThreadExecutor);
                        agentId = container.getContainerId();
                    }
                    break;
            }

            String finalAgentId = agentId;
            E2CMessage.E2C_HostInitializeReq finalHostInit = agentInit;
            future.thenAccept(result -> {
                if (result) {
                    CompletableFuture<Boolean> sysModuleFuture;
                    String hash = String.valueOf(Objects.hash(finalHostInit.getOsType(), finalHostInit.getAgentVersion()));
                    SysModule sysModule = sysModuleCacheMap.get(hash);
                    if (ObjectUtils.isEmpty(sysModule)) {
                        sysModuleFuture = saveSysModel(hash, finalHostInit.getOsType(), finalHostInit.getAgentVersion());
                    } else {
                        sysModuleFuture = CompletableFuture.completedFuture(true);
                    }
                    CompletableFuture<Boolean> gpuFuture = saveBatchGpus(finalHostInit, finalAgentId);

                    CompletableFuture<Boolean> cpuFuture = saveCpu(finalHostInit, finalAgentId);

                    CompletableFuture<Boolean> agentSessionFuture = saveAgentSession(finalHostInit, message, finalAgentId);

                    CompletableFuture<Void> allOfFuture = CompletableFuture.allOf(sysModuleFuture, gpuFuture, cpuFuture, agentSessionFuture);
                    allOfFuture.thenRun(() -> {
                        boolean sysModuleResult = sysModuleFuture.join();
                        boolean gpuResult = gpuFuture.join();
                        boolean cpuResult = cpuFuture.join();
                        boolean agentSessionResult = agentSessionFuture.join();

                        if (sysModuleResult && gpuResult && cpuResult && agentSessionResult) {
                            if (!ObjectUtils.isEmpty(parentNode)) {
                                parentNode.addOrUpdateNode(finalAgentId, RegisterNodeEnum.HOST, ctx.channel());
                            }

                            centerNode.printNodeInfoIterative();
                            monitor.updateHeartbeat(finalAgentId, RegisterNodeEnum.HOST, expirationTime -> offline(finalAgentId, agentType));
                            CommonMessageWrapper.CommonMessage returnMessage = CommonMessageWrapper.CommonMessage.newBuilder()
                                    .setHeader(CommonMessageWrapper.CommonMessage.newBuilder()
                                            .getHeaderBuilder()
                                            .setMethId(message.getHeader().getMethId())
                                            .setLinkSide(CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE)
                                            .setProtocolVer(CommonMessageWrapper.ENUM_PROTOCOL_VER.PROTOCOL_V1_VALUE)
                                            .setZone(message.getHeader().getZone())
                                            .setRegion(message.getHeader().getRegion())
                                            .setIdcSid(message.getHeader().getIdcSid())
                                            .setRelaySid(message.getHeader().getRelaySid())
                                            .setTraceId(message.getHeader().getTraceId()))
                                    .setBody(CommonMessageWrapper.Body.newBuilder().setCode(0)
                                            .setPayload(
                                                    C2EMessage.C2E_HostInitializeResp.newBuilder()
                                                            .setHostId(StringUtils.isEmpty(finalAgentId) ? finalHostInit.getHostId() : finalAgentId)
                                                            .setDevSn(finalHostInit.getDevSn())
                                                            .build().toByteString()
                                            ).build()).build();

                            ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(returnMessage.toByteArray())));
                        } else {
                            sendHostInitErrorResponse(ctx, finalHostInit.getDevSn(), message, x00000505.getValue(), x00000505.getMsg());
                        }

                    }).exceptionally(throwable -> {
                        sendHostInitErrorResponse(ctx, finalHostInit.getDevSn(), message, x00000505.getValue(), x00000505.getMsg());
                        KvLogger.instance(this)
                                .p(LogFieldConstants.ACTION, "InitSysModuleOrCpuInfoOrGpuInfoError")
                                .p(LogFieldConstants.ERR_MSG, throwable.getMessage())
                                .p("devSn", finalHostInit.getDevSn())
                                .e();
                        return null;
                    });
                }
            }).exceptionally(throwable -> {
                sendHostInitErrorResponse(ctx, finalHostInit != null ? finalHostInit.getDevSn() : "", message, x00000500.getValue(), x00000500.getMsg());
                KvLogger.instance(this)
                        .p(LogFieldConstants.ACTION, "InitHost")
                        .p(LogFieldConstants.ERR_MSG, throwable.getMessage())
                        .p("devSn", finalHostInit.getDevSn())
                        .e();
                return null;
            });
        } catch (Exception ex) {
            sendHostInitErrorResponse(ctx, agentInit != null ? agentInit.getDevSn() : "", message, x00000500.getValue(), x00000500.getMsg());
        }
    }

    /**
     * host exit
     *
     * @param ctx     ChannelHandlerContext
     * @param message CommonMessage
     */
    private void hostExit(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message) {
        try {
            ByteString payload = message.getBody().getPayload();
            E2CMessage.E2C_HostExitReq exitReq = E2CMessage.E2C_HostExitReq.parseFrom(payload);

            offline(exitReq.getHostId(), AgentTypeEnum.fromString(exitReq.getAgentType()));

            EdgeController.sendSuccessCommonMessage(ctx, message, x00000602);
        } catch (Exception ex) {
            sendErrorResponse(ctx, message, x00000500.getValue(), x00000500.getMsg());
        }
    }


    public boolean validateRegion(CommonMessageWrapper.Header header, String originZone, String originRegion, boolean updateFlag, String agentId, String agentIp, String localIp) {
        if (Objects.hash(header.getZone(), header.getRegion()) != (Objects.hash(originZone, originRegion))) {
            updateFlag = true;
            KvLogger.instance(this).p("HostId", agentId)
                    .p("originZone", originZone).p("currentZone", header.getZone())
                    .p("originIP", agentIp)
                    .p("originRegion", originRegion).p("currentRegion", header.getRegion())
                    .p("currentIP", localIp).w();
        }

        return updateFlag;
    }

    private CompletableFuture<Boolean> saveCpu(E2CMessage.E2C_HostInitializeReq agentInit, String agentId) {
        AtomicBoolean result = new AtomicBoolean(false);
        AtomicReference<CompletableFuture<Boolean>> cpuInfoFuture = new AtomicReference<>(CompletableFuture.completedFuture(true));
        cpuInfosCacheMap.compute(agentId, (key, value) -> {
            String shardKey = cpuInfoConsistentHash.getShard(agentId).toString();
            AgentCpu cpuInfoLocalCache = RedissonUtils.getLocalCache(shardKey, agentId);
            AtomicReference<AgentCpu> fianlCpu = new AtomicReference<>(cpuInfoLocalCache);
            if (ObjectUtils.isEmpty(cpuInfoLocalCache)) {
                if (!ObjectUtils.isEmpty(agentInit.getCpuSpec())) {
                    E2CMessage.CpuInfo cpuSpec = agentInit.getCpuSpec();
                    cpuInfoFuture.set(CompletableFuture.supplyAsync(() -> {
                        AgentCpu cpu = AgentCpu.builder()
                                .agentId(agentId)
                                .cpuNum(cpuSpec.getCpuNum())
                                .cpuType(cpuSpec.getCpuType())
                                .cpuManufacturer(cpuSpec.getCpuManufacturer())
                                .cpuArchitecture(cpuSpec.getCpuArchitecture())
                                .cpuCores(cpuSpec.getCpuCores())
                                .cpuThreads(cpuSpec.getCpuThreads())
                                .cpuBaseSpeed(cpuSpec.getCpuBaseSpeed())
                                .build();
                        result.set(agentCpuService.insert(cpu));
                        if (result.get()) {
                            cpuInfosCacheMap.put(agentId, cpu);
                            RedissonUtils.setLocalCachedMap(shardKey, agentId, cpu);
                            fianlCpu.set(cpu);
                        }
                        return result.get();
                    }, virtualThreadExecutor));
                }
            }
            return fianlCpu.get();
        });
        return cpuInfoFuture.get();
    }

    public CompletableFuture<Boolean> saveSysModel(String hash, String osType, String agentVersion) {
        return CompletableFuture.supplyAsync(() -> {
            String shardKey = sysModuleConsistentHash.getShard(hash).toString();
            SysModule sysModule = RedissonUtils.getLocalCache(shardKey, hash);
            boolean result = true;
            if (ObjectUtils.isEmpty(sysModule)) {
                SysModule module = SysModule.builder()
                        .moduleId(UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.ROOT))
                        .moduleName(osType)
                        .moduleArch(osType)
                        .version(agentVersion)
                        .build();
                result = sysModuleService.insert(module);
                if (result) {
                    sysModuleCacheMap.put(hash, sysModule);
                    RedissonUtils.setLocalCachedMap(shardKey, hash, module);
                }
            }
            return result;
        }, virtualThreadExecutor);
    }

    private CompletableFuture<Boolean> saveBatchGpus(E2CMessage.E2C_HostInitializeReq agentInit, String agentId) {
        AtomicReference<CompletableFuture<Boolean>> gpuInfoFuture = new AtomicReference<>(CompletableFuture.completedFuture(true));
        gpuInfosCacheMap.compute(agentId, (k, v) -> {
            if (CollectionUtils.isEmpty(v)) {
                String shardKey = gpuInfoConsistentHash.getShard(agentId).toString();
                List<AgentGpu> agentGpus = RedissonUtils.getLocalCache(shardKey, agentId);
                AtomicReference<List<AgentGpu>> finalAgentGpus = new AtomicReference<>(agentGpus);
                if (CollectionUtils.isEmpty(finalAgentGpus.get())) {
                    gpuInfoFuture.set(CompletableFuture.supplyAsync(() -> {
                        finalAgentGpus.set(getAgentGpus(agentInit, agentId));
                        boolean result = agentGpuService.saveBatch(finalAgentGpus.get());
                        if (result) {
                            gpuInfosCacheMap.put(agentId, finalAgentGpus.get());
                            RedissonUtils.setLocalCachedMap(shardKey, agentId, finalAgentGpus.get());
                        }
                        return result;
                    }, virtualThreadExecutor));
                }
                return finalAgentGpus.get();
            }
            return v;
        });
        return gpuInfoFuture.get();
    }

    private CompletableFuture<Boolean> saveAgentSession(E2CMessage.E2C_HostInitializeReq agentInit, CommonMessageWrapper.CommonMessage message, String agentId) {
        AtomicReference<CompletableFuture<Boolean>> agentSessionFuture = new AtomicReference<>(CompletableFuture.completedFuture(true));
        agentSessionCacheMap.compute(agentId, (k, v) -> {
            if (ObjectUtils.isEmpty(v)) {
                String shardKey = agentSessionConsistentHash.getShard(agentId).toString();
                AgentSession agentSession = RedissonUtils.getLocalCache(shardKey, agentId);
                AtomicReference<AgentSession> finalAgentSession = new AtomicReference<>(agentSession);
                if (ObjectUtils.isEmpty(finalAgentSession.get())) {
                    agentSessionFuture.set(CompletableFuture.supplyAsync(() -> {
                        finalAgentSession.set(AgentSession.builder()
                                .agentId(agentId)
                                .zone(message.getHeader().getZone())
                                .region(message.getHeader().getRegion())
                                .idc(message.getHeader().getIdcSid())
                                .agentType(agentInit.getAgentType())
                                .containerCount(NumberUtils.INTEGER_ZERO)
                                .build());
                        boolean result = agentSessionService.save(finalAgentSession.get());
                        if (result) {
                            agentSessionCacheMap.put(agentId, finalAgentSession.get());
                            RedissonUtils.setLocalCachedMap(shardKey, agentId, finalAgentSession.get());
                        }
                        return result;
                    }, virtualThreadExecutor));
                }
                return finalAgentSession.get();
            }
            return v;
        });
        return agentSessionFuture.get();
    }


    private static List<AgentGpu> getAgentGpus(E2CMessage.E2C_HostInitializeReq agentInit, String agentId) {
        List<E2CMessage.GpuInfo> gpuInfos = agentInit.getGpuListList();
        List<AgentGpu> agentGpus = new ArrayList<>(gpuInfos.size());
        gpuInfos.forEach(x -> agentGpus.add(
                AgentGpu.builder()
                        .agentId(agentId)
                        .gpuType(x.getGpuType())
                        .gpuManufacturer(x.getGpuManufacturer())
                        .gpuMem(String.valueOf(x.getGpuMem()))
                        .gpuBusType(x.getGpuBusType())
                        .gpuBusId(x.getGpuBusId())
                        .gpuDeviceId(x.getGpuDeviceId())
                        .build()));
        return agentGpus;
    }

    /**
     *
     * send error response
     * @author yijian

     * @date 2024/12/17 10:44
     */
    private void sendErrorResponse(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message, int code, String msg) {
        CommonMessageWrapper.CommonMessage errorMessage =
                CommonMessageWrapper.CommonMessage.newBuilder().setHeader(message.getHeader().toBuilder()
                                .setLinkSide(CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE))
                        .setBody(CommonMessageWrapper.Body.newBuilder().setCode(code).setMsg(msg)).build();
        ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(errorMessage.toByteArray())));
    }

    private void sendHostInitErrorResponse(ChannelHandlerContext ctx, String devSn, CommonMessageWrapper.CommonMessage message, int code, String msg) {
        CommonMessageWrapper.CommonMessage errorMessage =
                CommonMessageWrapper.CommonMessage.newBuilder().setHeader(message.getHeader().toBuilder()
                                .setLinkSide(CommonMessageWrapper.ENUM_LINK_SIDE.CENTER_TO_EDGE_VALUE))
                        .setBody(CommonMessageWrapper.Body.newBuilder().setCode(code).setMsg(msg)
                                .setPayload(C2EMessage.C2E_HostInitializeResp.newBuilder()
                                        .setDevSn(devSn)
                                        .build().toByteString()).build()).build();
        ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(errorMessage.toByteArray())));
    }


    /**
     * Edge ping
     *
     * @param ctx     ChannelHandlerContext
     * @param message CommonMessage
     */
    private void ping(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message) {
//        try {
//            ByteString payload = message.getBody().getPayload();
//            CommonMessageWrapper.Header header = message.getHeader();
//            String serviceId = !StringUtils.isEmpty(header.getIdcSid()) ? header.getIdcSid() : header.getRelaySid();
//            E2CMessage.E2C_HostHeartbeatReq hostHeartbeatReq = E2CMessage.E2C_HostHeartbeatReq.parseFrom(payload);
//            List<E2CMessage.HostHbData> hbData = hostHeartbeatReq.getHbDataList();
//            if (CollectionUtils.isEmpty(hbData)) {
//                sendErrorResponse(ctx, message, x00000412.getValue(), x00000412.getMsg());
//            }
//
//            Node node = findNodeByServiceId(serviceId);
//            hbData.forEach(x -> {
//                String hostId = x.getHostId();
//                if (!ObjectUtils.isEmpty(node)) {
//                    node.addOrUpdateNode(x.getHostId(), RegisterNodeEnum.HOST, ctx.channel());
//                }
//                AgentTypeEnum agentType = AgentTypeEnum.fromString(x.getAgentType());
//                AgentSession session = agentSessionCacheMap.get(x.getHostId());
//                session.setCpuUsage(x.getHostStatus().getCpuUsage());
//                session.setMemoryUsage(x.getHostStatus().getMemoryUsage());
//                session.setGpuUsage(x.getHostStatus().getGpuUsage());
//                session.setGpuFanSpeed(x.getHostStatus().getGpuFanSpeed());
//                session.setGpuTemperature(x.getHostStatus().getGpuTemperature());
//                switch (agentType) {
//                    case HOST:
//                        Host host = hostCurrentMap.get(x.getHostId());
//                        session.setContainerCount(Integer.parseInt(x.getVmStatusList().parallelStream().filter(y -> y.getRunning()).count() + ""));
//                        session.setAgentIp(host.getHostIp());
//                        session.setResourcePool(host.getResourcePool());
//
//                        if (ObjectUtils.isEmpty(host)) {
//                            host.setLastHbAt(new Date());
//                            hostCurrentMap.put(x.getHostId(), host);
//                            String hostShardKey = hostConsistentHash.getShard(hostId).toString();
//                            RedissonUtils.setLocalCachedMap(hostShardKey, hostId, host);
//                            monitor.updateHeartbeat(x.getHostId(), RegisterNodeEnum.HEARTBEAT, expirationTime -> heartbeat(x.getHostId(), AgentTypeEnum.HOST));
//                            monitor.updateHeartbeat(x.getHostId(), RegisterNodeEnum.HOST, expirationTime -> offline(x.getHostId(), AgentTypeEnum.HOST));
//                        }
//                        List<E2CMessage.VmStatus> vmStatusList = x.getVmStatusList();
//                        if (!CollectionUtils.isEmpty(vmStatusList)) {
//                            ConcurrentHashMap<String, Boolean> runningAgentMap = vmStatusList.stream().collect(Collectors.toMap(E2CMessage.VmStatus::getCid, E2CMessage.VmStatus::getRunning, (key1, key2) -> key2, ConcurrentHashMap::new));
//                            if (!ObjectUtils.isEmpty(runningAgentMap)) {
//                                Iterator<Map.Entry<String, Boolean>> iterator = runningAgentMap.entrySet().iterator();
//                                while (iterator.hasNext()) {
//                                    Map.Entry<String, Boolean> entry = iterator.next();
//                                    String containerId = entry.getKey();
//                                    Container container = containerCurrentMap.get(containerId);
//                                    AgentSession containerAgentSession = agentSessionCacheMap.get(containerId);
//                                    BeanUtils.copyProperties(session, containerAgentSession);
//                                    containerAgentSession.setAgentIp(container.getContainerIp());
//                                    containerAgentSession.setResourcePool(container.getResourcePool());
//
//                                    containerCurrentMap.put(containerId, container);
//                                    String containerShardKey = containerConsistentHash.getShard(containerId).toString();
//                                    RedissonUtils.setLocalCachedMap(containerShardKey, containerId, container);
//
//                                    agentSessionCacheMap.put(containerId, containerAgentSession);
//                                    if (entry.getValue()) {
//                                        monitor.updateHeartbeat(x.getHostId(), RegisterNodeEnum.HEARTBEAT, expirationTime -> heartbeat(containerId, AgentTypeEnum.CONTAINER));
//                                        monitor.updateHeartbeat(x.getHostId(), RegisterNodeEnum.HOST, expirationTime -> offline(containerId, AgentTypeEnum.CONTAINER));
//                                    }
//                                }
//                            }
//                        }
//                        break;
//                    case CONTAINER, BENCHMARK:
//                        String containerId = x.getHostId();
//                        Container container = containerCurrentMap.get(containerId);
//                        session.setAgentIp(container.getContainerIp());
//                        session.setResourcePool(container.getResourcePool());
//
//                        if (container != null) {
//                            container.setLastHbAt(new Date());
//                            containerCurrentMap.put(hostId, container);
//                            String containerShardKey = containerConsistentHash.getShard(containerId).toString();
//                            RedissonUtils.setLocalCachedMap(containerShardKey, containerId, container);
//                        }
//                        monitor.updateHeartbeat(x.getHostId(), RegisterNodeEnum.HEARTBEAT, expirationTime -> heartbeat(containerId, AgentTypeEnum.CONTAINER));
//                        monitor.updateHeartbeat(x.getHostId(), RegisterNodeEnum.HOST, expirationTime -> offline(containerId, AgentTypeEnum.CONTAINER));
//                        break;
//                }
//                agentSessionCacheMap.put(x.getHostId(), session);
//
//
//            });
//
//            EdgeController.sendSuccessCommonMessage(ctx, message);
//
//        } catch (Exception ex) {
//            sendErrorResponse(ctx, message, x00000500.getValue(), x00000500.getMsg());
//        }
    }

    public static String convertToHex(int regionId) {
        String hex = Integer.toHexString(regionId).toUpperCase();
        return String.format("%4s", hex).replace(' ', '0');
    }

    /**
     *
     * timeout offline
     * @author yijian
     * @date 2025/1/3 17:43
     */
    public void heartbeat(String hostId, AgentTypeEnum agentType) {
        CompletableFuture.runAsync(() -> {
            try {
                updateAgentSession(hostId, agentType);
            } catch (Exception e) {
                KvLogger.instance(SpringContextHolder.getBean(EdgeController.class))
                        .p(LogFieldConstants.ACTION, "OfflineError")
                        .p(LogFieldConstants.ERR_MSG, e.getMessage())
                        .p("params", hostId)
                        .e();
            }
        }, virtualThreadExecutor);
    }

    /**
     *
     * timeout offline
     * @author yijian
     * @date 2025/1/3 17:43
     */
    public void offline(String agentId, AgentTypeEnum agentTypeEnum) {
        CompletableFuture.runAsync(() -> {
            try {
                Node node = findNodeByServiceId(agentId);
                if (node == null) {
                    return;
                }
                switch (agentTypeEnum) {
                    case HOST -> {
                        AgentSession agentSession = agentSessionCacheMap.get(agentId);
                        agentSession.setContainerCount(NumberUtils.INTEGER_ZERO);
                        agentSessionCacheMap.put(agentId, agentSession);
                        String agentSessionShardKey = agentSessionConsistentHash.getShard(agentId).toString();
                        RedissonUtils.setLocalCachedMap(agentSessionShardKey, agentId, agentSession);
                    }
                }
                node.removeNode();
                centerNode.printNodeInfoIterative();
                node.getChannel().disconnect();
            } catch (Exception e) {
                KvLogger.instance(SpringContextHolder.getBean(EdgeController.class))
                        .p(LogFieldConstants.ACTION, "OfflineAgentError")
                        .p(LogFieldConstants.ERR_MSG, e.getMessage())
                        .p("params", agentId)
                        .p("AgentTypeEnum", agentTypeEnum)
                        .e();
            }
        }, virtualThreadExecutor);
    }

    private void updateAgentSession(String agentId, AgentTypeEnum agentType) {
        ContainerService cService = SpringContextHolder.getBean(ContainerService.class);
        HostService hsService = SpringContextHolder.getBean(HostService.class);
        AgentSessionService agSessionService = SpringContextHolder.getBean(AgentSessionService.class);
        if (!ObjectUtils.isEmpty(agentId)) {
            AgentSession agentSession = agentSessionCacheMap.get(agentId);
            boolean update = false;
            switch (agentType) {
                case HOST -> update = hsService.update(new LambdaUpdateWrapper<Host>()
                        .set(Host::getLastHbAt, hostCurrentMap.get(agentId).getLastHbAt())
                        .eq(Host::getHostId, agentId));

                case CONTAINER -> update = cService.update(new LambdaUpdateWrapper<Container>()
                        .set(Container::getLastHbAt, containerCurrentMap.get(agentId).getLastHbAt())
                        .eq(Container::getContainerId, agentId));
            }

            agSessionService.update(agentSession);
            if (!update) {
                KvLogger.instance(SpringContextHolder.getBean(EdgeController.class))
                        .p(LogFieldConstants.ACTION, "Heartbeat")
                        .p(LogFieldConstants.ERR_MSG, "HeartbeatUpdateError")
                        .p("HostId", agentId)
                        .p("UpdateResult", update)
                        .e();
            }
        }
    }
}
