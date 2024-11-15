package com.nhnacademy.twojopinggateway.filter;

import com.nhnacademy.twojopinggateway.client.UserInfoClient;
import com.nhnacademy.twojopinggateway.interceptor.FeignRequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.*;

@Component
@Slf4j
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {

    private final UserInfoClient userInfoClient;

    public CustomFilter(@Lazy final UserInfoClient userInfoClient) {
        super(Config.class);
        this.userInfoClient = userInfoClient;
    }

    @Override
    public GatewayFilter apply(Config config) {

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        return (exchange, chain) -> {
            if (exchange.getRequest().getPath().toString().startsWith("/login")) {
                return chain.filter(exchange);
            }
            String[] cookies = exchange.getRequest().getHeaders().getFirst(HttpHeaders.COOKIE).split("; ");
            for (String cookie : cookies) {
                if (cookie.startsWith("accessToken=")) {
                    System.setProperty("accessToken", cookie);
                }
            }
            Future<Map<String,String>> future = executorService.submit(userInfoClient::getUserInfo);
            try {
                Map<String, String> map = future.get();
                exchange.getRequest().mutate()
                        .header("X-Customer-Id", map.get("id"))
                        .header("X-Customer-Role", map.get("role"))
                        .build();

                return chain.filter(exchange);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        };
    }


    public static class Config {
    }
}
