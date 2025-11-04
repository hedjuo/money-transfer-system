package com.acme.bank.txeventpublisher.application.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.acme.bank.payments.infrastructure.persistence.entity.jpa")
public class PersistenceConfig {
}
