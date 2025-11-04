package com.acme.bank.txeventpublisher.application.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.ReactiveRedisLockProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;

@Configuration
public class SchedulerConfig {
    @Bean
    public LockProvider lockProvider(ReactiveRedisConnectionFactory connectionFactory) {
        return new ReactiveRedisLockProvider.Builder(connectionFactory)
                .environment("dev")
                .build();
    }
}
