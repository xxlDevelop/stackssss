package org.yx.hoststack.edge.server.http;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.yx.hoststack.edge.server.http.handler.HealthHandler;


@Configuration
@RequiredArgsConstructor
public class RouterFunctionConfig {

    private final HealthHandler healthHandler;

    @Bean
    public RouterFunction<ServerResponse> routingFunction() {
        RouterFunctions.Builder globalRouterBuilder = RouterFunctions.route();
        return globalRouterBuilder
                .path("host-stack-edge", baseUrlBuilder -> baseUrlBuilder
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                                .GET("/ops/health", healthHandler::health)
                        )
                        .filter((request, next) -> next.handle(request))
                )
                .build();
    }
}
