package com.nhnacademy.twojopinggateway.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.server.ServerWebExchange;

@Component
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String cookie =  System.getProperty("accessToken");

        if (cookie != null) {
            requestTemplate.header(HttpHeaders.COOKIE, "accessToken="+cookie);
        }
    }

}
