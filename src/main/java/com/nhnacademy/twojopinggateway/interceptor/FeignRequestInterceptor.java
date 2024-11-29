package com.nhnacademy.twojopinggateway.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Component
public class FeignRequestInterceptor implements RequestInterceptor {



    @Override
    public void apply(RequestTemplate requestTemplate) {
        String cookie = (String) RequestContextHolder.getRequestAttributes().getAttribute("accessToken", RequestAttributes.SCOPE_REQUEST);

        if (cookie != null) {
            requestTemplate.header(HttpHeaders.COOKIE, "accessToken="+cookie);
        }
    }

}
