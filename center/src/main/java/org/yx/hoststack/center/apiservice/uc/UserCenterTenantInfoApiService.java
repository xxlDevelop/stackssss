package org.yx.hoststack.center.apiservice.uc;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.yx.hoststack.center.apiservice.ApiServiceBase;
import org.yx.hoststack.center.apiservice.uc.resp.TenantInfoResp;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;

@Service
@Slf4j
public class UserCenterTenantInfoApiService extends ApiServiceBase {

    public UserCenterTenantInfoApiService(WebClient webClient) {
        super(webClient);
    }

    public Mono<TenantInfoResp> checkAuthToken(String payload ) {
        return route(
                "https://api.example.com",
                "trace-" + UUID.randomUUID(),
                HttpMethod.POST,
                Collections.emptyMap(),
                payload
        ).doOnSubscribe(subscription -> log.info("Starting API call")).flatMap(response -> {
            try {
                return Mono.just(JSONObject.parseObject(response, TenantInfoResp.class));
            } catch (Exception e) {
                log.error("Failed to parse response", e);
                return Mono.error(new RuntimeException("Response parsing failed"));
            }
        }).doOnSuccess(parsedResponse -> log.info("API call completed successfully: {}", parsedResponse)).doOnError(error -> log.error("API call failed", error));
    }
}
