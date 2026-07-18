package com.raj.gateway.config;

import com.raj.gateway.filter.RequestValidationFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder, RequestValidationFilter requestValidationFilter) {

        return builder.routes()
                // Identity Service
                .route("identity-service", route -> route
                        .path("/api/v1/users/**")
                        .uri("lb://identity-service")
                )

                // Wallet Service
                .route("wallet-service", route -> route
                        .path("/api/v1/wallets/**")
                        .filters(filter -> filter
                                .filter(requestValidationFilter.validateRequest()))
                        .uri("lb://wallet-service")
                )
                .build();
    }
}