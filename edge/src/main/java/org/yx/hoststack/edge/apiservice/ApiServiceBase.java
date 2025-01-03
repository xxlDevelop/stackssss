package org.yx.hoststack.edge.apiservice;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.yx.hoststack.common.syscode.EdgeSysCode;
import org.yx.hoststack.edge.config.WebClientConfig;
import org.yx.lib.utils.util.R;
import org.yx.lib.utils.util.StringUtil;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApiServiceBase {

    private final WebClient webClient;

    public Mono<String> post(String postUrl, String traceId, Map<String, String> customerHeaders, Object requestBody) {
        return route(postUrl, traceId, HttpMethod.GET, customerHeaders, requestBody);
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
        Map<String, String> finalCustomerHeaders = customerHeaders;
        String finalTraceId = traceId;
        return webClient.method(httpMethod)
                .uri(url)
                .headers(httpHeaders -> httpHeaders.addAll(MultiValueMap.fromSingleValue(finalCustomerHeaders)))
                .exchangeToMono(WebClientConfig.ReactorExecutor.execute(url, httpMethod.name(), customerHeaders, requestBody))
                .doOnError(throwable -> WebClientConfig.ReactorExecutor.throwBizExp(url, httpMethod.name(), throwable))
                .onErrorReturn(JSON.toJSONString(R.builder()
                        .code(EdgeSysCode.HttpCallFailed.getValue())
                        .msg(EdgeSysCode.HttpCallFailed.getMsg())
                        .time(System.currentTimeMillis())
                        .build()))
                .contextWrite(contextWare -> contextWare.put("traceId", finalTraceId));
    }

}
