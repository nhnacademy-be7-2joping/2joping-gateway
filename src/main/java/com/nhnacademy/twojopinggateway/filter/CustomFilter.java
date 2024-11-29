package com.nhnacademy.twojopinggateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.twojopinggateway.client.UserInfoClient;
import com.nhnacademy.twojopinggateway.dto.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@Component
@Slf4j
public class CustomFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {

    private final ObjectMapper objectMapper;

    private final UserInfoClient userInfoClient;

    public CustomFilter(@Lazy final UserInfoClient userInfoClient, ObjectMapper objectMapper) {
        super(Config.class);
        this.userInfoClient = userInfoClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {

        ExecutorService executorService = Executors.newFixedThreadPool(10);
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
            System.setProperty("accessToken", cookies.getFirst("accessToken").getValue());
            RequestContextHolder.getRequestAttributes().setAttribute("accessToken", cookies.getFirst("accessToken").getValue(), RequestAttributes.SCOPE_REQUEST);

            Future<Map<String,String>> future = executorService.submit(userInfoClient::getUserInfo);
            try {
                Map<String, String> map = future.get();
                exchange.getRequest().mutate()
                        .header("X-Customer-Id", map.get("id"))
                        .header("X-Customer-Role", map.get("role"))
                        .build();
                //2안
//                exchange.getResponse().getHeaders().add("X-Customer-Id", map.get("id"));
//                exchange.getResponse().getHeaders().add("X-Customer-Role", map.get("role"));
                return chain.filter(exchange);
            } catch (InterruptedException | ExecutionException e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                ErrorResponseDto errorResponseDto = new ErrorResponseDto("TOKEN_EXPIRED", "accessToken expired");
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
        };
    }


    public static class Config {
    }
}
