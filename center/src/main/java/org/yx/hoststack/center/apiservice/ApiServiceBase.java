package org.yx.hoststack.center.apiservice;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.yx.hoststack.center.common.config.WebClientConfig;
import org.yx.lib.utils.util.StringUtil;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Service
public class ApiServiceBase {
    private final WebClient webClient;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final Duration INITIAL_BACKOFF = Duration.ofSeconds(1);
    private static final Duration MAX_BACKOFF = Duration.ofSeconds(10);

    public ApiServiceBase(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> post(String postUrl, String traceId, Map<String, String> customerHeaders, Object requestBody) {
        return route(postUrl, traceId, HttpMethod.POST, customerHeaders, requestBody);
    }

    public Mono<String> get(String getUrl, String traceId, Map<String, String> customerHeaders) {
        return route(getUrl, traceId, HttpMethod.GET, customerHeaders, null);
    }

    public Mono<String> route(String url, String traceId, HttpMethod httpMethod, Map<String, String> customerHeaders, Object requestBody) {
        if (customerHeaders == null) {
            customerHeaders = Maps.newHashMap();
        }
        if (StringUtil.isBlank(traceId)) {
            traceId = UUID.fastUUID().toString();
        }

        customerHeaders.putIfAbsent("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        Map<String, String> finalCustomerHeaders = customerHeaders;
        String finalTraceId = traceId;

        WebClient.RequestBodySpec requestBodySpec = webClient.method(httpMethod)
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(MultiValueMap.fromSingleValue(finalCustomerHeaders)));

        WebClient.RequestHeadersSpec<?> headersSpec;
        if (httpMethod == HttpMethod.POST && requestBody != null) {
            String jsonBody = requestBody instanceof String ? (String) requestBody : JSON.toJSONString(requestBody);
            headersSpec = requestBodySpec.body(BodyInserters.fromValue(jsonBody));
        } else {
            headersSpec = requestBodySpec;
        }

        return headersSpec
                .exchangeToMono(WebClientConfig.ReactorExecutor.execute(url, httpMethod.name(), finalCustomerHeaders, requestBody))
                .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, INITIAL_BACKOFF)
                        .maxBackoff(MAX_BACKOFF)
                        .filter(this::shouldRetry))
                .doOnError(throwable ->
                        WebClientConfig.ReactorExecutor.throwBizExp(url, httpMethod.name(), throwable))
                .contextWrite(contextWare -> contextWare.put("traceId", finalTraceId));
    }

    private boolean shouldRetry(Throwable throwable) {
        return throwable instanceof java.net.SocketTimeoutException || throwable instanceof java.net.SocketException || throwable instanceof WebClientResponseException && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError();
    }
}