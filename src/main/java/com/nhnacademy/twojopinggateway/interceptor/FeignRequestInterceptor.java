package com.nhnacademy.twojopinggateway.interceptor;

import com.nhnacademy.twojopinggateway.context.ThreadLocalContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class FeignRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        String accessToken = ThreadLocalContext.getAccessToken();
        if (accessToken != null) {
            requestTemplate.header(HttpHeaders.COOKIE, "accessToken=" + accessToken);
        }
    }
}
