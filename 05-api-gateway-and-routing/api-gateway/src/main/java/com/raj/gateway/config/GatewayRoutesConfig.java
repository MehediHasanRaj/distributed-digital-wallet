package com.raj.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {

        return builder.routes()
                // Identity Service
                .route("identity-service", route -> route
                        .path("/api/v1/users/**")
                        .uri("lb://identity-service")
                )

                // Wallet Service
                .route("wallet-service", route -> route
                        .path("/api/v1/wallets/**")
                        .uri("lb://wallet-service")
                )
                .build();
    }
}