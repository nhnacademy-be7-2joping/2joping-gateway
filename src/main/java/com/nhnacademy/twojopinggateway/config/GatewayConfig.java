package com.nhnacademy.twojopinggateway.config;

import com.nhnacademy.twojopinggateway.client.UserInfoClient;
import com.nhnacademy.twojopinggateway.filter.CustomFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class GatewayConfig {

    private final CustomFilter customFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {



        CustomFilter.Config config = new CustomFilter.Config();

        return builder.routes()
                .route("auth_service", r -> r.path("/auth/**", "/login")

                        .filters(f -> f.filter(customFilter.apply(config)))
                        .uri("lb://TWOJOPING-USER-AUTH"))// fill service here...
                .route("bookstore_service", r -> r.path("/api/v1/**")
                        .uri("lb://BOOKSTORE")) // fill service here...
                //... 위 형식으로 서비스 계속 추가할 것.
                .build();
    }

//    @Bean
//    public GlobalFilter customGlobalFilter() {
//        RequestUserIn
//    }

}
