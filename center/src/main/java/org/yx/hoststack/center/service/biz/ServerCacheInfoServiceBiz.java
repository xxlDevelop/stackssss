package org.yx.hoststack.center.service.biz;

import io.netty.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.yx.hoststack.center.common.dto.ServiceDetailDTO;
import org.yx.hoststack.center.common.enums.RegisterNodeEnum;
import org.yx.hoststack.center.entity.*;
import org.yx.hoststack.center.service.IdcInfoService;
import org.yx.hoststack.center.service.RelayInfoService;
import org.yx.hoststack.center.service.ServiceDetailService;
import org.yx.hoststack.center.ws.common.Node;

import java.util.Optional;

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


    public ServiceDetailDTO getServiceInfo(String nodeId) {
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
}