package com.acme.bank.application.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class ApiKeyResolversConfig {
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> Mono.just(
                exchange.getRequest().getHeaders().getFirst("X-Api-Key") != null
                        ? exchange.getRequest().getHeaders().getFirst("X-Api-Key").split(",")[0].trim()
                        : null
        );
    }
}
