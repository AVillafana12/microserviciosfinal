package com.clinic.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class GlobalCorsConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter corsFilter() {
        return (ServerWebExchange ctx, WebFilterChain chain) -> {
            ServerHttpRequest request = ctx.getRequest();
            ServerHttpResponse response = ctx.getResponse();
            HttpHeaders headers = response.getHeaders();
            
            // Obtener el origen de la petición
            String origin = request.getHeaders().getFirst(HttpHeaders.ORIGIN);
            
            // Configurar headers CORS
            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin != null ? origin : "*");
            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "Authorization, Content-Type, Accept, X-Requested-With");
            headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization, Content-Type");
            headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
            
            // Manejar preflight OPTIONS
            if (HttpMethod.OPTIONS.equals(request.getMethod())) {
                response.setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }
            
            return chain.filter(ctx);
        };
    }
}
