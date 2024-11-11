package com.nhnacademy.twojopinggateway.filter;

import com.nhnacademy.twojopinggateway.service.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtAuthenticationFilter implements GlobalFilter {

    @Autowired
    private JwtTokenService jwtTokenService;
    @Autowired
    private WebClient.Builder webClientBuilder;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //로그인 Path는 로직없이 가능
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/mypage")) {
            // 토큰 추출
            List<String> tokens = jwtTokenService.resolveToken(exchange);
            String accessToken = tokens.get(0);
            String refreshToken = tokens.get(1);

            // accessToken validation
            if (accessToken != null && jwtTokenService.validateToken(accessToken)) {
                Authentication authentication = jwtTokenService.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                exchange.getRequest().mutate().header("X-Custom-Header", String.valueOf(jwtTokenService.getCustomerId(accessToken)));
                return chain.filter(exchange);
            }

            // if accessToken is expired... then validate refreshToken and request accessToken to auth Server
            if (refreshToken != null && jwtTokenService.validateToken(refreshToken)) {
                Authentication authentication = jwtTokenService.getAuthentication(refreshToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                return requestNewAccessToken(refreshToken, exchange, chain);
            }

            // if both tokens are expired... redirect to login page...
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setLocation(URI.create("/auth/login"));
            return response.setComplete();
        }
        return chain.filter(exchange);
    }

    private Mono<Void> requestNewAccessToken(String refreshToken, ServerWebExchange exchange, GatewayFilterChain chain) {

        // 리프레시 토큰을 사용해 인증 서버로 요청
        return webClientBuilder.build()
                .get()
                .uri("http://localhost:8083/auth/refreshToken")
                .cookie("refreshToken", refreshToken)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class).flatMap(errorResponse -> {
                            // 4xx 에러 발생 시 로깅하고 에러 반환
                            System.err.println("Error refreshing token: " + errorResponse);
                            return Mono.error(new RuntimeException("Failed to refresh token: " + errorResponse));
                        }))
                .bodyToMono(String.class) // 응답을 문자열로 받음
                .flatMap(tokenResponse -> {
                    // 쿠키에 새로운 토큰이 설정되었으면 리다이렉트 수행
                    exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER); // 303 리다이렉트 코드
                    exchange.getResponse().getHeaders().setLocation(URI.create("http://localhost:8083/auth/refreshToken"));
                    exchange.getRequest().mutate().header("X-Custom-Header", String.valueOf(jwtTokenService.getCustomerId(refreshToken)));
                    return exchange.getResponse().setComplete();
                })
                .doOnError(error -> {
                    // 에러 발생 시 로깅
                    System.err.println("Request failed: " + error.getMessage());
                });
    }
}
