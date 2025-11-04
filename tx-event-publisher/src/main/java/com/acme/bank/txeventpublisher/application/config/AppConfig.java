package com.acme.bank.txeventpublisher.application.config;

import com.acme.bank.txeventpublisher.infrastructure.persistence.repository.DefaultTransactionEventRepository;
import com.acme.bank.txeventpublisher.domain.TransactionEventRepository;
import com.acme.bank.txeventpublisher.infrastructure.persistence.repository.JpaTransactionEventRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AppConfig {
    @Bean
    public TransactionEventRepository txEventRepository(JpaTransactionEventRepository delegate) {
        return new DefaultTransactionEventRepository(delegate);
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
