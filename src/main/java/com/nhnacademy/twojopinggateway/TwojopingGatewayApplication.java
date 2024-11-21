package com.nhnacademy.twojopinggateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class TwojopingGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(TwojopingGatewayApplication.class, args);
	}

}
