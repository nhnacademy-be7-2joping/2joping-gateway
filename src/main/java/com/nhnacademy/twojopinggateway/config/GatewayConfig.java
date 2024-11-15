package com.nhnacademy.twojopinggateway.config;

// import com.nhnacademy.twojopinggateway.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    // @Autowired
    // private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth_service", r -> r.path("/auth/**", "/login")
                        .uri("lb://TWOJOPING-USER-AUTH"))// fill service here...
                .route("bookstore_service", r -> r.path("/api/v1/**")
                        .uri("lb://BOOKSTORE")) // fill service here...
                //... 위 형식으로 서비스 계속 추가할 것.
                .build();
    }

//    @Bean
//    public GlobalFilter jwtGlobalFilter() {
//        return jwtAuthenticationFilter; // JwtAuthenticationFilter를 글로벌 필터로 등록
//    }

}
