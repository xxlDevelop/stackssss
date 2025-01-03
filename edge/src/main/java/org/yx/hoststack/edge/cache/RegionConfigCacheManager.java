package org.yx.hoststack.edge.cache;

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.yx.hoststack.edge.common.CacheKeyConstants;
import org.yx.hoststack.edge.common.EdgeContext;
import org.yx.hoststack.edge.cache.model.RegionCoturnConfig;
import org.yx.hoststack.edge.cache.model.RegionStorageConfig;
import org.yx.hoststack.protocol.ws.server.C2EMessage;

@Component
@RequiredArgsConstructor
public class RegionConfigCacheManager {
    private final RedissonClient redissonClient;

    public void setRegionConfig(C2EMessage.C2E_RegionConfigSyncReq regionConfigSyncReq) {
        RegionStorageConfig storageConfig = RegionStorageConfig.builder()
                .type(regionConfigSyncReq.getStorage().getType())
                .config(regionConfigSyncReq.getStorage().getConfig())
                .build();
        redissonClient.getBucket(String.format(CacheKeyConstants.RegionStorageConfig, EdgeContext.Region)).set(JSON.toJSONString(storageConfig));

        RegionCoturnConfig coturnConfig = RegionCoturnConfig.builder()
                .serverSvc(regionConfigSyncReq.getCoturn().getServerSvc())
                .serverUser(regionConfigSyncReq.getCoturn().getServerUser())
                .serverPwd(regionConfigSyncReq.getCoturn().getServerPwd())
                .build();
        redissonClient.getBucket(String.format(CacheKeyConstants.RegionCoturnConfig, EdgeContext.Region)).set(JSON.toJSONString(coturnConfig));
    }

    public RegionStorageConfig getRegionStorageConfig(String region) {
        Object value = redissonClient.getBucket(String.format(CacheKeyConstants.RegionStorageConfig, EdgeContext.Region)).get();
        if (value == null) {
            return null;
        } else {
            return JSON.parseObject(value.toString(), RegionStorageConfig.class);
        }
    }

    public RegionCoturnConfig getRegionCoturnConfig(String region) {
        Object value = redissonClient.getBucket(String.format(CacheKeyConstants.RegionCoturnConfig, EdgeContext.Region)).get();
        if (value == null) {
            return null;
        } else {
            return JSON.parseObject(value.toString(), RegionCoturnConfig.class);
        }
    }
}
