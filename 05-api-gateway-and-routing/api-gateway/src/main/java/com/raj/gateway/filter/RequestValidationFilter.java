package com.raj.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RequestValidationFilter {

    public GatewayFilter validateRequest() {

        return (exchange, chain) -> {
            log.info("Validating request {}", exchange.getRequest().getURI());

            return chain.filter(exchange);

        };
    }
}