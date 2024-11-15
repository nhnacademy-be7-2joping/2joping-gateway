package com.nhnacademy.twojopinggateway.client;

import com.nhnacademy.twojopinggateway.interceptor.FeignRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(name = "TWOJOPING-USER-AUTH", configuration = FeignRequestInterceptor.class)
public interface UserInfoClient {
    @GetMapping("/auth/user-info")
    Map<String, String> getUserInfo();
}
