package org.yx.hoststack.center.ws.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.protobuf.ByteString;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.yx.hoststack.center.common.enums.RegisterModeEnum;
import org.yx.hoststack.center.entity.*;
import org.yx.hoststack.center.service.*;
import org.yx.hoststack.center.ws.controller.manager.CenterControllerManager;
import org.yx.hoststack.protocol.ws.server.C2EMessage;
import org.yx.hoststack.protocol.ws.server.CommonMessageWrapper;
import org.yx.hoststack.protocol.ws.server.E2CMessage;
import org.yx.hoststack.protocol.ws.server.ProtoMethodId;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.yx.hoststack.center.common.enums.SysCode.*;


/**
 * Process Center Basic Message
 */
@Service("WsHostController")
@RequiredArgsConstructor
public class HostController {
    {
        CenterControllerManager.add(ProtoMethodId.HostInitialize, this::init);
        CenterControllerManager.add(ProtoMethodId.HostHeartbeat, this::ping);
    }

    private ConcurrentHashMap<String, Host> hostCurrentMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Container> containerCurrentMap = new ConcurrentHashMap<>();


    private final RegionInfoService regionInfoService;
    private final ServiceDetailService serviceDetailService;
    private final TenantInfoService tenantInfoService;
    private final HostService hostService;
    private final HostGpuService hostGpuService;
    private final HostCpuService hostCpuService;
    private final SysModuleService sysModuleService;
    private final ContainerService containerService;
    private final AgentSessionService agentSessionService;

    /**
     * host initialize
     *
     * @param ctx     ChannelHandlerContext
     * @param message CommonMessage
     */
    private void init(ChannelHandlerContext ctx, CommonMessageWrapper.CommonMessage message) {
        try {
            ByteString payload = message.getBody().getPayload();
            E2CMessage.E2C_HostInitializeReq hostInit = E2CMessage.E2C_HostInitializeReq.parseFrom(payload);

            Long tenantId = message.getHeader().getTenantId();
            TenantInfo tenantInfo = tenantInfoService.getById(tenantId);
            if (ObjectUtils.isEmpty(tenantInfo)) {
                sendErrorResponse(ctx, message, x00000409.getValue(), x00000409.getMsg());
            }
            RegionInfo regionInfo = regionInfoService.getOne(new LambdaQueryWrapper<RegionInfo>().eq(RegionInfo::getRegionCode, message.getHeader().getRegion()).eq(RegionInfo::getZoneCode, message.getHeader().getZone()), false);
            if (ObjectUtils.isEmpty(regionInfo)) {
                sendErrorResponse(ctx, message, x00000410.getValue(), x00000410.getMsg());
            }
            if (!ObjectUtils.isEmpty(hostInit.getHostId()) && !containerService.exists(new LambdaQueryWrapper<Container>().eq(Container::getHostId, hostInit.getHostId()))) {
                sendErrorResponse(ctx, message, x00000411.getValue(), x00000411.getMsg());
            }

            RegisterModeEnum mode = RegisterModeEnum.fromString(hostInit.getRegisterMode());
            String hostId = StringUtils.EMPTY;
            switch (mode) {
                case HOST:
                    if (ObjectUtils.isEmpty(hostCurrentMap.get(hostInit.getDevSn()))) {
                        Host host = Host.builder().hostId(convertToHex(regionInfo.getRegionId()) + DigestUtils.md5Hex(hostInit.getDevSn()))
                                .hostVersion(hostInit.getAgentVersion())
                                .startTime(new Date(hostInit.getOsStartTs()))
                                .devSn(hostInit.getDevSn())
                                .osType(hostInit.getOsType())
                                .osVersion(hostInit.getOsVersion())
                                .osMem(String.valueOf(hostInit.getOsMem()))
                                .resourcePool(hostInit.getResourcePool())
                                .runtimeEnv(hostInit.getRuntimeEnv())
                                .diskInfo(hostInit.getDisk())
                                .networkCardInfo(JSONObject.toJSONString(hostInit.getNetCardListList()))
                                .zone(message.getHeader().getZone())
                                .region(message.getHeader().getRegion())
                                .idc(message.getHeader().getIdcSid())
                                .hostIp(hostInit.getLocalIp())
                                .gpuNum(hostInit.getGpuListCount())
                                .cpuNum(hostInit.getCpuSpec().getCpuNum())
                                .baremetalProvider(String.valueOf(message.getHeader().getTenantId()))
                                .lastHbAt(new Date())
                                .ak(tenantInfo.getTenantAk())
                                .sk(tenantInfo.getTenantSk())
                                .build();
                        hostId = host.getHostId();

                        hostService.insert(host);
                    }
                    break;
                case CONTAINER, BENCHMARK:
                    if (ObjectUtils.isEmpty(containerCurrentMap.get(hostInit.getDevSn()))) {
//                        Container container = Container.builder()
//                                .containerId(convertToHex(regionInfo.getRegionId()) + DigestUtils.md5Hex(hostInit.getDevSn()))
////                                .name("")
////                                .label("customer")
////                                .status("normal")
////                                .hostId(hostInit.getHostId())
////                                .bizType(hostInit.getAgentType())
////                                .resourcePool(hostInit.getResourcePool())
////                                .osType(hostInit.getOsType())
////                                .vGpu(hostInit.getGpuListCount())
////                                .memory(hostInit.getOsMem())
////                                .zone(message.getHeader().getZone())
////                                .region(message.getHeader().getRegion())
////                                .idc(message.getHeader().getIdcSid())
////                                .contianerType(hostInit.getRuntimeEnv())
//                                .lastHbAt(new Date())
//                                .build();
////                        containerService.insert(container);
//                        containerCurrentMap.put(hostInit.getDevSn(), container);
                    }
                    break;
            }

//            if (!ObjectUtils.isEmpty(hostInit.getCpuSpec())) {
//                E2CMessage.CpuInfo cpuSpec = hostInit.getCpuSpec();
//                hostCpuService.insert(HostCpu.builder()
//                        .hostId(host.getHostId())
//                        .cpuNum(cpuSpec.getCpuNum())
//                        .cpuType(cpuSpec.getCpuType())
//                        .cpuManufacturer(cpuSpec.getCpuManufacturer())
//                        .cpuArchitecture(cpuSpec.getCpuArchitecture())
//                        .cpuCores(cpuSpec.getCpuCores())
//                        .cpuThreads(cpuSpec.getCpuThreads())
//                        .cpuBaseSpeed(cpuSpec.getCpuBaseSpeed())
//                        .build());
//            }
//            if (!CollectionUtils.isEmpty(hostInit.getGpuListList())) {
//                List<HostGpu> hostGpus = getHostGpus(hostInit, host);
//                hostGpuService.saveBatch(hostGpus);
//            }

            SysModule sysModule = sysModuleService.getOne(new LambdaQueryWrapper<SysModule>().eq(SysModule::getModuleArch, hostInit.getOsType()).eq(SysModule::getVersion, hostInit.getAgentVersion()), false);
            if (ObjectUtils.isEmpty(sysModule)) {
                sysModuleService.insert(SysModule.builder()
                        .moduleId(UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.ROOT))
                        .moduleName(hostInit.getOsType())
                        .moduleArch(hostInit.getOsType())
                        .version(hostInit.getAgentVersion())
                        .build());
            }

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
                                            .setHostId(StringUtils.isEmpty(hostId) ? hostInit.getHostId() : hostId)
                                            .build().toByteString()
                            ).build()).build();

            ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(returnMessage.toByteArray())));
        } catch (Exception ex) {
            sendErrorResponse(ctx, message, x00000500.getValue(), x00000500.getMsg());
        }
    }

//    @NotNull
    private static List<HostGpu> getHostGpus(E2CMessage.E2C_HostInitializeReq hostInit, Host host) {
        List<E2CMessage.GpuInfo> gpuInfos = hostInit.getGpuListList();
        List<HostGpu> hostGpus = new ArrayList<>(gpuInfos.size());
        gpuInfos.forEach(x -> {
            hostGpus.add(HostGpu.builder()
                    .hostId(host.getHostId())
                    .gpuType(x.getGpuType())
                    .gpuManufacturer(x.getGpuManufacturer())
                    .gpuMem(String.valueOf(x.getGpuMem()))
                    .gpuBusType(x.getGpuBusType())
                    .gpuBusId(x.getGpuBusId())
                    .gpuDeviceId(x.getGpuDeviceId())
                    .build());
        });
        return hostGpus;
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
            ByteString payload = message.getBody().getPayload();
            E2CMessage.E2C_HostHeartbeatReq hostHeartbeatReq = E2CMessage.E2C_HostHeartbeatReq.parseFrom(payload);
            List<E2CMessage.HostHbData> hbData = hostHeartbeatReq.getHbDataList();
            if (CollectionUtils.isEmpty(hbData)) {
                sendErrorResponse(ctx, message, x00000412.getValue(), x00000412.getMsg());
            }
            hbData.forEach(x -> {
                RegisterModeEnum mode = RegisterModeEnum.fromString(x.getAgentType());
                AgentSession session = agentSessionService.getOne(new LambdaQueryWrapper<AgentSession>().eq(AgentSession::getAgentId, x.getHostId()), false);
                switch (mode) {
                    case HOST:
                        if (ObjectUtils.isEmpty(session.getAgentId())) {
                             session = AgentSession.builder()
                                    .agentId(x.getHostId())
                                    .zone(message.getHeader().getZone())
                                    .region(message.getHeader().getRegion())
                                    .idc(message.getHeader().getIdcSid())
                                    .agentType(x.getAgentType())
                                    .resourcePool(hostCurrentMap.get(x.getHostId()).getResourcePool())
                                    .containerCount(x.getVmStatusCount())
                                    .cpuUsage(x.getHostStatus().getCpuUsage())
                                    .memoryUsage(x.getHostStatus().getMemoryUsage())
                                    .build();

                        }
                        agentSessionService.saveOrUpdate(session);
                    case CONTAINER, BENCHMARK:

                }
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

    public static String convertToHex(int regionId) {
        String hex = Integer.toHexString(regionId).toUpperCase();
        return String.format("%4s", hex).replace(' ', '0');
    }
}
