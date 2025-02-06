package org.yx.hoststack.center.service.biz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.yx.hoststack.center.apiservice.ApiServiceBase;
import org.yx.hoststack.center.common.dto.ServiceDetailDTO;
import org.yx.hoststack.center.common.enums.RegisterNodeEnum;
import org.yx.hoststack.center.common.req.channel.SendChannelBasic;
import org.yx.hoststack.center.entity.*;
import org.yx.hoststack.center.service.IdcInfoService;
import org.yx.hoststack.center.service.RelayInfoService;
import org.yx.hoststack.center.service.ServiceDetailService;
import org.yx.hoststack.center.service.center.CenterService;
import org.yx.hoststack.center.ws.common.Node;
import org.yx.lib.utils.constant.CommonConstants;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;

import java.util.Optional;

import static org.yx.hoststack.center.common.constant.CenterEvent.Action.CREATE_IMAGE_POST_REMOTE_SUCCESS;
import static org.yx.hoststack.center.common.constant.CenterEvent.Action.FETCH_CHANNEL_FROM_REMOTE_FAILED;
import static org.yx.hoststack.center.common.constant.CenterEvent.CREATE_IMAGE_EVENT;
import static org.yx.hoststack.center.ws.common.Node.findNodeByServiceId;
import static org.yx.hoststack.center.ws.controller.EdgeController.*;
import static org.yx.hoststack.center.ws.controller.HostController.containerCurrentMap;
import static org.yx.hoststack.center.ws.controller.HostController.hostCurrentMap;

/**
 * @author lyc
 * @since 2024-12-09 15:15:18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ServerCacheInfoServiceBiz {

    private final RelayInfoService relayInfoService;
    private final IdcInfoService idcInfoService;
    private final ServiceDetailService serviceDetailService;
    CenterService centerService;
    private final ApiServiceBase apiServiceBase;


    /**
     * Priority search for local memory
     *
     * @author yijian
     * @date 2025/1/22 11:22
     */
    //TODO 查询idc或者relay信息，本地没有查询redis
    public ServiceDetailDTO getNodeInfo(String nodeId) {
        if (ObjectUtils.isEmpty(nodeId)) {
            return null;
        }

        ServiceDetailDTO dto = new ServiceDetailDTO();

        RelayInfo relayInfo = relayInfoCacheMap.get(nodeId);
        IdcInfo idcInfo = idcInfoCacheMap.get(nodeId);

        Optional<ServiceDetail> first = serverDetailCacheMap.values().parallelStream()
                .filter(x -> x.getEdgeId().equals(nodeId)).findFirst();

        if (first.isPresent()) {

            ServiceDetail detail = first.get();
            Node node = findNodeByServiceId(detail.getServiceId());

            dto.setNodeId(detail.getEdgeId());
            dto.setZone(getZone(idcInfo, relayInfo));
            dto.setRegion(getRegion(idcInfo, relayInfo));
            dto.setServiceId(detail.getServiceId());

            if (detail.getType().equalsIgnoreCase(RegisterNodeEnum.RELAY.name())) {
                dto.setRelaySid(detail.getServiceId());
                dto.setType(String.valueOf(RegisterNodeEnum.RELAY));
            } else {
                dto.setType(String.valueOf(RegisterNodeEnum.IDC));
                dto.setIdcSid(detail.getServiceId());
                if (!ObjectUtils.isEmpty(node) && !RegisterNodeEnum.CENTER.equals(node.getParent().getType())) {
                    ServiceDetailDTO parentServiceDetailDTO = getParentServiceDetailDTO(node);
                    dto.setServiceId(parentServiceDetailDTO.getServiceId());
                    dto.setParent(parentServiceDetailDTO);
                    dto.setRelaySid(detail.getServiceId());
                }
            }
            return dto;
        } else {
            return null;
        }
    }

    /**
     * Priority search for local memory, unable to find remote search return
     *
     * @author yijian
     * @date 2025/1/22 11:21
     */
    public ServiceDetailDTO getAgentInfo(String hostId) {
        if (ObjectUtils.isEmpty(hostId)) {
            return null;
        }

        Node node = findNodeByServiceId(hostId);
        if (ObjectUtils.isEmpty(node)) {
            SendChannelBasic req = SendChannelBasic.builder().hostId(hostId).build();
            String postUrl = centerService.buildRemoteUrl(req, "");
            return apiServiceBase.post(postUrl, MDC.get(CommonConstants.TRACE_ID), centerService.prepareRequestHeaders(), JSON.toJSONString(req))
                    .map(result -> result != null ? JSONObject.parseObject(result, ServiceDetailDTO.class) : null)
                    .doOnError(e -> logError(e, postUrl, req))
                    .doOnNext(r -> logSuccess(r, postUrl, req)).block();
        }
        ServiceDetailDTO dto = new ServiceDetailDTO();
        Host host = hostCurrentMap.get(hostId);
        Container container = containerCurrentMap.get(hostId);
        dto.setHostId(hostId);
        dto.setZone(host != null ? host.getZone() : container.getZone());
        dto.setRegion(host != null ? host.getRegion() : container.getRegion());

        ServiceDetail detail = serverDetailCacheMap.get(node.getServiceId());
        dto.setType(String.valueOf(RegisterNodeEnum.HOST));
        dto.setNodeId(detail.getEdgeId());

        if (detail.getType().equalsIgnoreCase(RegisterNodeEnum.RELAY.name())) {
            dto.setRelaySid(detail.getServiceId());
        } else {
            dto.setIdcSid(detail.getServiceId());
            if (!ObjectUtils.isEmpty(node) && !RegisterNodeEnum.CENTER.equals(node.getParent().getType())) {
                ServiceDetailDTO parentServiceDetailDTO = getParentServiceDetailDTO(node);
                dto.setParent(parentServiceDetailDTO);
                dto.setRelaySid(detail.getServiceId());
            }
        }
        return dto;
    }


    private String getZone(IdcInfo idcInfo, RelayInfo relayInfo) {
        return idcInfo != null ? idcInfo.getZone() : relayInfo.getZone();
    }

    private String getRegion(IdcInfo idcInfo, RelayInfo relayInfo) {
        return idcInfo != null ? idcInfo.getRegion() : relayInfo.getRegion();
    }

    private ServiceDetailDTO getParentServiceDetailDTO(Node node) {
        ServiceDetailDTO parentDto = new ServiceDetailDTO();
        ServiceDetail parentDetail = serverDetailCacheMap.get(node.getParent().getServiceId());

        if (!ObjectUtils.isEmpty(parentDetail)) {
            RelayInfo parentRelay = relayInfoCacheMap.get(parentDetail.getEdgeId());
            parentDto.setServiceId(parentDetail.getServiceId());
            parentDto.setType(String.valueOf(RegisterNodeEnum.RELAY));
            parentDto.setNodeId(parentRelay.getRelay());
            parentDto.setZone(parentRelay.getZone());
            parentDto.setRegion(parentRelay.getRegion());
            parentDto.setRelaySid(parentDetail.getServiceId());
        }
        return parentDto;
    }


    public Host getHostInfo(String hosId) {
        return hostCurrentMap.getOrDefault(hosId, null);
    }

    public Container getContainerInfo(String containerId) {
        return containerCurrentMap.getOrDefault(containerId, null);
    }

    /**
     * get channel info by serviceId
     *
     * @param serviceId 服务ID
     * @return 通道信息
     */
    public Channel getServiceChanelInfo(String serviceId) {
        Node node = findNodeByServiceId(serviceId);
        if (ObjectUtils.isEmpty(node)) {
            return null;
        }
        return node.getChannel();
    }


    private void logError(Throwable e, String remoteUrl, Object req) {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, CREATE_IMAGE_EVENT)
                .p(LogFieldConstants.ACTION, FETCH_CHANNEL_FROM_REMOTE_FAILED)
                .p(LogFieldConstants.ERR_MSG, e.getMessage())
                .p(LogFieldConstants.Alarm, 0)
                .p(LogFieldConstants.API_URL, remoteUrl)
                .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                .p(LogFieldConstants.TRACE_ID, MDC.get(CommonConstants.TRACE_ID))
                .e(e);
    }

    private void logSuccess(Object r, String remoteUrl, Object req) {
        KvLogger.instance(this)
                .p(LogFieldConstants.EVENT, CREATE_IMAGE_EVENT)
                .p(LogFieldConstants.ACTION, CREATE_IMAGE_POST_REMOTE_SUCCESS)
                .p(LogFieldConstants.API_URL, remoteUrl)
                .p(LogFieldConstants.ReqData, JSON.toJSONString(req))
                .p(LogFieldConstants.RespData, JSON.toJSONString(r))
                .p(LogFieldConstants.TRACE_ID, MDC.get(CommonConstants.TRACE_ID))
                .i();
    }
}