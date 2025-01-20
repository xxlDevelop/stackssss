package org.yx.hoststack.center.service.biz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yx.hoststack.center.entity.Container;
import org.yx.hoststack.center.entity.Host;
import org.yx.hoststack.center.entity.IdcInfo;
import org.yx.hoststack.center.entity.RelayInfo;

import static org.yx.hoststack.center.ws.controller.EdgeController.idcInfoCacheMap;
import static org.yx.hoststack.center.ws.controller.EdgeController.relayInfoCacheMap;
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

    public RelayInfo getRelayInfo(String relaySid) {
        return relayInfoCacheMap.getOrDefault(relaySid,null);
    }

    public IdcInfo getIdcInfo(String idcSid) {
        return idcInfoCacheMap.getOrDefault(idcSid, null);
    }


    public Host getHostInfo(String hosId) {
        return hostCurrentMap.getOrDefault(hosId, null);
    }

    public Container getContainerInfo(String containerId) {
        return containerCurrentMap.getOrDefault(containerId, null);
    }

}