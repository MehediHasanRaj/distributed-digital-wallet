package com.raj.gateway.filter;

import com.raj.gateway.util.Headers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class CorrelationIdFilter
        implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain)
    {

        String correlationId = exchange.getRequest()
                        .getHeaders()
                        .getFirst(Headers.CORRELATION_ID);

        // if not exist, generate one
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        String finalCorrelationId = correlationId;
        ServerWebExchange mutatedExchange =
                exchange.mutate()
                        .request(builder ->
                                builder.header(
                                        Headers.CORRELATION_ID, finalCorrelationId)
                        )
                        .build();
        long start = System.currentTimeMillis();

        return chain.filter(mutatedExchange).then(
                Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - start;
                    log.info(
                            "[{}] {} {} {} ms", finalCorrelationId, exchange.getRequest().getMethod()
                            ,exchange.getRequest().getURI(), duration
                    );
                })
        );

    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

}