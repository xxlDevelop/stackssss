package org.yx.hoststack.edge.config;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.yx.hoststack.common.syscode.EdgeSysCode;
import org.yx.hoststack.edge.common.exception.InvalidHttpStatusException;
import org.yx.lib.utils.logger.KvLogger;
import org.yx.lib.utils.logger.LogFieldConstants;
import org.yx.lib.utils.util.StringUtil;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

@Configuration
public class WebClientConfig {

    @Value("${webClient.maxConnections:500}")
    private int maxConnections;

    @Value("${webClient.maxIdleTime:3}")
    private int maxIdleTime;

    @Value("${webClient.connectTimeout:5}")
    private int connectTimeout;

    @Value("${webClient.responseTimeout:5}")
    private int responseTimeout;

    @Value("${webClient.readTimeout:5}")
    private int readTimeout;

    @Value("${webClient.writeTimeout:5}")
    private int writeTimeout;

    @Bean
    public WebClient webClient() {
        // Config http connectPool
        ConnectionProvider provider = ConnectionProvider.builder("custom")
                .maxConnections(maxConnections)
                .maxIdleTime(Duration.ofSeconds(maxIdleTime))
                .build();

        // config http client
        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout * 1000)
                .responseTimeout(Duration.ofSeconds(responseTimeout))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(readTimeout))
                                .addHandlerLast(new WriteTimeoutHandler(writeTimeout)));

        // build webClient instance
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
                            return Mono.error(new InvalidHttpStatusException(clientResponse.statusCode().value(), body, traceId,
                                    StringUtil.isBlank(body) ? EdgeSysCode.HttpCallFailed.getMsg() : body));
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
