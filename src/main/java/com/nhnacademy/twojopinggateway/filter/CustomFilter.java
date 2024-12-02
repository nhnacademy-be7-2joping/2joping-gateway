package com.nhnacademy.twojopinggateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.twojopinggateway.client.UserInfoClient;
import com.nhnacademy.twojopinggateway.context.ThreadLocalContext;
import com.nhnacademy.twojopinggateway.dto.ErrorResponseDto;
import com.nhnacademy.twojopinggateway.util.ThreadUtil;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Component
@Slf4j
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {

    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    private final UserInfoClient userInfoClient;

    public CustomFilter(@Lazy final UserInfoClient userInfoClient, ObjectMapper objectMapper,
                        ExecutorService executorService) {
        super(Config.class);
        this.executorService = executorService;
        this.userInfoClient = userInfoClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            if (!exchange.getRequest().getCookies().containsKey("accessToken")) {
                // TODO 토큰이 없는 상태인데 회원만 접속 가능한 루트에 접속시 401 에러 반환
                return chain.filter(exchange);
            }
            //1안
            if (exchange.getRequest().getPath().toString().equals("/auth/user-info")) {
                return chain.filter(exchange);
            }

            MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();
            String accessToken = cookies.getFirst("accessToken").getValue();

            return Mono.deferContextual(context -> {
                        Future<Map<String, String>> future =
                                executorService.submit(() -> {
                                    ThreadLocalContext.setAccessToken(accessToken);
                                    return userInfoClient.getUserInfo();
                                });
                        return Mono.fromFuture(ThreadUtil.toCompletableFuture(future))
                                .flatMap(userInfo -> {
                                    // 헤더 추가
                                    exchange.getRequest().mutate()
                                            .header("X-Customer-Id", userInfo.get("id"))
                                            .header("X-Customer-Role", userInfo.get("role"))
                                            .build();
                                    return chain.filter(exchange);
                                });
                    })
                    .doFinally(signalType -> ThreadLocalContext.clear())
                    .onErrorResume(e -> {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        ErrorResponseDto errorResponseDto = new ErrorResponseDto("TOKEN_EXPIRED", e.getMessage());
                        try {
                            byte[] responseBody = objectMapper.writeValueAsBytes(errorResponseDto);
                            return exchange.getResponse().writeWith(
                                    Mono.just(exchange.getResponse()
                                                      .bufferFactory()
                                                      .wrap(responseBody))
                            );
                        } catch (JsonProcessingException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
        };
    }

    @PreDestroy
    public void shutDownExecutorService() {
        log.info("Shutting down executor service");
        executorService.shutdown();
    }

    public static class Config {
    }
}
