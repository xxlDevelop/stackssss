package org.yx.hoststack.center.common.config;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.yx.hoststack.common.syscode.EdgeSysCode;
import org.yx.hoststack.center.common.exception.InvalidHttpStatusException;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient() {
        // 配置HTTP连接池
        ConnectionProvider provider = ConnectionProvider.builder("center-http-pool")
                .maxConnections(500)
                .maxIdleTime(Duration.ofSeconds(20))
                .build();

        // 配置HTTP客户端
        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(5))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5))
                                .addHandlerLast(new WriteTimeoutHandler(5)));

        // 构建WebClient实例
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public static class ReactorExecutor {
        public static Function<ClientResponse, Mono<String>> execute(String url, String method,
                                                                     Map<String, String> httpRequestHeaders, Object reqBody) {
            long startTs = System.currentTimeMillis();
            KvLogger httpLogger = KvLogger.instance(ReactorExecutor.class)
                    .p(LogFieldConstants.EVENT, "Remoting")
                    .p(LogFieldConstants.ACTION, "Request")
                    .p(LogFieldConstants.CostMs, System.currentTimeMillis() - startTs)
                    .p("ReqMethod", method)
                    .p(LogFieldConstants.API_URL, url);
            if (reqBody != null) {
                httpLogger.p(LogFieldConstants.ReqData, JSON.toJSONString(reqBody));
            }
            if (httpRequestHeaders != null) {
                httpRequestHeaders.forEach((name, value) -> httpLogger.p("H-" + name, value));
            }
            return (clientResponse -> clientResponse.bodyToMono(String.class)
                    .flatMap(body -> Mono.deferContextual(contextView -> {
                        String traceId = contextView.get("traceId").toString();
                        httpLogger.p(LogFieldConstants.TRACE_ID, traceId);
                        httpLogger.p(LogFieldConstants.Code, clientResponse.statusCode().value())
                                .i();
//                        StringUtils.abbreviate("",100)
                        if (clientResponse.statusCode().value() != HttpStatus.OK.value()) {
                            return Mono.error(new InvalidHttpStatusException(clientResponse.statusCode().value(), body, traceId, EdgeSysCode.HttpCallFailed.getMsg()));
                        } else {
                            return Mono.just(body);
                        }
                    })));
        }

        public static void throwBizExp(String url, String method, Throwable throwable) {
            KvLogger kvLogger = KvLogger.instance(ReactorExecutor.class)
                    .p(LogFieldConstants.EVENT, "Remoting")
                    .p(LogFieldConstants.ACTION, "Request")
                    .p(LogFieldConstants.API_URL, url)
                    .p(LogFieldConstants.Code, EdgeSysCode.HttpCallFailed.getValue())
                    .p(LogFieldConstants.ERR_MSG, EdgeSysCode.HttpCallFailed.getMsg())
                    .p("ReqMethod", method);
            if (throwable instanceof InvalidHttpStatusException invalidHttpStatusException) {
                kvLogger.p("HttpStatus", invalidHttpStatusException.getStatus())
                        .p("HttpResponse", invalidHttpStatusException.getBody())
                        .p(LogFieldConstants.TRACE_ID, invalidHttpStatusException.getTraceId())
                        .e(throwable);
            } else {
                kvLogger.e(throwable);
            }
        }
    }
}
