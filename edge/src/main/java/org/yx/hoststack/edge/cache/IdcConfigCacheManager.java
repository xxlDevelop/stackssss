package org.yx.hoststack.edge.cache;

import cn.hutool.core.map.MapBuilder;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.yx.hoststack.edge.common.CacheKeyConstants;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.cache.model.IdcBasicConfig;
import org.yx.hoststack.edge.cache.model.IdcNetConfig;
import org.yx.hoststack.protocol.ws.server.C2EMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class IdcConfigCacheManager {
    private final RedissonClient redissonClient;

    public void setIdcBasicConfig(C2EMessage.EdgeBasicConfig basic) {
        IdcBasicConfig idcBasicConfig = IdcBasicConfig.builder()
                .localShareStorageHttpSvc(basic.getLocalShareStorageHttpSvc())
                .shareStorageUser(basic.getShareStorageUser())
                .shareStoragePwd(basic.getShareStoragePwd())
                .localLogSvcHttpSvc(basic.getLocalLogSvcHttpSvc())
                .netLogSvcHttpsSvc(basic.getNetLogSvcHttpsSvc())
                .speedTestSvc(basic.getSpeedTestSvc())
                .location(basic.getLocation())
                .build();
        redissonClient.getBucket(String.format(CacheKeyConstants.IdcBasicConfigObj, EdgeContext.IdcId)).set(JSON.toJSONString(idcBasicConfig));
        Map<String, String> configMap = MapBuilder.create(new HashMap<String, String>())
                .put(IdcBasicConfig.LOCAL_SHARE_STORAGE_HTTP_SVC, idcBasicConfig.getLocalShareStorageHttpSvc())
                .put(IdcBasicConfig.SHARE_STORAGE_USER, idcBasicConfig.getShareStorageUser())
                .put(IdcBasicConfig.SHARE_STORAGE_PWD, idcBasicConfig.getShareStorageUser())
                .put(IdcBasicConfig.LOCAL_LOG_SVC_HTTP_SVC, idcBasicConfig.getLocalLogSvcHttpSvc())
                .put(IdcBasicConfig.NET_LOG_SVC_HTTPS_SVC, idcBasicConfig.getNetLogSvcHttpsSvc())
                .put(IdcBasicConfig.SPEED_TEST_SVC, idcBasicConfig.getSpeedTestSvc())
                .put(IdcBasicConfig.LOCATION, idcBasicConfig.getLocation())
                .build();
        redissonClient.getMap(String.format(CacheKeyConstants.IdcBasicConfigMap, EdgeContext.IdcId)).putAll(configMap);
    }

    public String getIdcBasicConfig(String configKey) {
        Object configValue = redissonClient.getMap(String.format(CacheKeyConstants.IdcBasicConfigMap, EdgeContext.IdcId)).get(configKey);
        return configValue == null ? "" : configValue.toString();
    }

    public void setIdcNetConfig(List<C2EMessage.EdgeNetConfig> netList) {
        List<IdcNetConfig> idcNetConfigs = Lists.newArrayList();
        for (C2EMessage.EdgeNetConfig edgeNetConfig : netList) {
            idcNetConfigs.add(IdcNetConfig.builder()
                    .localIp(edgeNetConfig.getLocalIp())
                    .localPort(edgeNetConfig.getLocalPort())
                    .mappingIp(edgeNetConfig.getMappingIp())
                    .mappingPort(edgeNetConfig.getMappingPort())
                    .netProtocol(edgeNetConfig.getNetProtocol())
                    .bandwidthInLimit(edgeNetConfig.getBandwidthInLimit())
                    .bandwidthOutLimit(edgeNetConfig.getBandwidthOutLimit())
                    .netIspType(edgeNetConfig.getNetIspType())
                    .ipType(edgeNetConfig.getIpType())
                    .build());
        }
        redissonClient.getBucket(String.format(CacheKeyConstants.IdcNetConfig, EdgeContext.IdcId)).set(JSON.toJSONString(idcNetConfigs));
    }

}
