// package com.nhnacademy.twojopinggateway.filter;

// import com.nhnacademy.twojopinggateway.service.JwtTokenService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.cloud.gateway.filter.GatewayFilterChain;
// import org.springframework.cloud.gateway.filter.GlobalFilter;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.HttpStatusCode;
// import org.springframework.http.server.reactive.ServerHttpResponse;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.stereotype.Component;
// import org.springframework.web.reactive.function.client.WebClient;
// import org.springframework.web.server.ServerWebExchange;
// import reactor.core.publisher.Mono;

// import java.net.URI;
// import java.util.List;

// @Component
// public class JwtAuthenticationFilter implements GlobalFilter {

//     @Autowired
//     private JwtTokenService jwtTokenService;
//     @Autowired
//     private WebClient.Builder webClientBuilder;

//     @Override
//     public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

//         //로그인 Path는 로직없이 가능
//         String path = exchange.getRequest().getURI().getPath();
//         if (path.startsWith("/login") || path.startsWith("/api/v1/members") || path.startsWith("/api/v1")) {
//             return chain.filter(exchange);
//         }

//         // 토큰 추출
//         List<String> tokens = jwtTokenService.resolveToken(exchange);
//         String accessToken = tokens.get(0);
//         String refreshToken = tokens.get(1);

//         // accessToken validation
//         if (accessToken != null && jwtTokenService.validateToken(accessToken)) {
//             Authentication authentication = jwtTokenService.getAuthentication(accessToken);
//             SecurityContextHolder.getContext().setAuthentication(authentication);
//             return chain.filter(exchange);
//         }

//         // if accessToken is expired... then validate refreshToken and request accessToken to auth Server
//         if (refreshToken != null && jwtTokenService.validateToken(refreshToken)) {
//             Authentication authentication = jwtTokenService.getAuthentication(refreshToken);
//             SecurityContextHolder.getContext().setAuthentication(authentication);
//             return requestNewAccessToken(refreshToken, exchange, chain);
//         }

//         // if both tokens are expired... redirect to login page...
//         ServerHttpResponse response = exchange.getResponse();
//         response.setStatusCode(HttpStatus.UNAUTHORIZED);
//         response.getHeaders().setLocation(URI.create("/auth/login"));
//         return response.setComplete();
//     }

//     private Mono<Void> requestNewAccessToken(String refreshToken, ServerWebExchange exchange, GatewayFilterChain chain) {
//         //auth 서버에 새로운 accessToken 요청
//         return webClientBuilder.build()
//                 .get()
//                 .uri("http://auth/refreshToken")
//                 .cookie("refreshToken", refreshToken)
//                 .retrieve()
//                 .onStatus(HttpStatusCode::is4xxClientError, response ->
//                         Mono.error(new RuntimeException("Failed to refresh token")))
//                 .bodyToMono(Void.class)
//                 .flatMap(response -> chain.filter(exchange));
//     }
// }
