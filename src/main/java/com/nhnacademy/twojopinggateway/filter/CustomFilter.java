package com.nhnacademy.twojopinggateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.twojopinggateway.client.UserInfoClient;
import com.nhnacademy.twojopinggateway.dto.ErrorResponseDto;
import com.nhnacademy.twojopinggateway.service.JwtTokenService;
import io.jsonwebtoken.ExpiredJwtException;
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

import java.util.concurrent.ExecutorService;

@Component
@Slf4j
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {

    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    private final JwtTokenService jwtTokenService;

    public CustomFilter(ObjectMapper objectMapper,
                        ExecutorService executorService,
                        JwtTokenService jwtTokenService) {
        super(Config.class);
        this.executorService = executorService;
        this.objectMapper = objectMapper;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!exchange.getRequest().getCookies().containsKey("accessToken")) {
                return chain.filter(exchange);
            }

            //1안
            if (exchange.getRequest().getPath().toString().equals("/auth/user-info")) {
                return chain.filter(exchange);
            }

            MultiValueMap<String, HttpCookie> cookies = exchange.getRequest().getCookies();
            String accessToken = cookies.getFirst("accessToken").getValue();

            try {
                jwtTokenService.validateToken(accessToken);
            } catch (ExpiredJwtException e) {
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
            }
            exchange.getRequest().mutate()
                    .header("X-Customer-Id", String.valueOf(jwtTokenService.getCustomerId(accessToken)))
                    .header("X-Customer-Role", jwtTokenService.getRole(accessToken))
                    .build();
            return chain.filter(exchange);
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
