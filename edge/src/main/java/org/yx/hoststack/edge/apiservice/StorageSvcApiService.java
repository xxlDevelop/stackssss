package org.yx.hoststack.edge.apiservice;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.yx.hoststack.edge.cache.IdcConfigCacheManager;
import reactor.core.publisher.Mono;

@Service
public class StorageSvcApiService extends ApiServiceBase {
    private final IdcConfigCacheManager idcConfigCacheManager;

    public StorageSvcApiService(WebClient webClient, IdcConfigCacheManager idcConfigCacheManager) {
        super(webClient);
        this.idcConfigCacheManager = idcConfigCacheManager;
    }

    public Mono<String> createBucket() {
        return Mono.empty();
    }

    public Mono<String> deleteBucket() {
        return Mono.empty();
    }

    public Mono<String> getOssAccessToken() {
        return Mono.empty();
    }

    public Mono<String> distributeFile() {
        return Mono.empty();
    }

    public Mono<String> createBaseVolume() {
        return Mono.empty();
    }

    public Mono<String> deleteBaseVolume() {
        return Mono.empty();
    }

    public Mono<String> mountBaseVolume() {
        return Mono.empty();
    }

    public Mono<String> createUserVolume() {
        return Mono.empty();
    }

    public Mono<String> deleteUserVolume() {
        return Mono.empty();
    }

    public Mono<String> mountUserVolume() {
        return Mono.empty();
    }
}
