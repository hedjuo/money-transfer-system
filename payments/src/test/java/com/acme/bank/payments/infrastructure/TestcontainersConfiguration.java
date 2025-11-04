package com.acme.bank.payments.infrastructure;

import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;

public class TestcontainersConfiguration {
    @Bean
    public GenericContainer<?> redisContainer() {
        var container = new GenericContainer<>("redis:latest");
        container.withExposedPorts(6379);
        container.start();

        System.setProperty("spring.data.redis.port", container.getMappedPort(6379).toString());

        return container;
    }
}
