package com.acme.bank.txeventpublisher;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
@EntityScan(basePackages = {
    "com.acme.bank.payments.infrastructure.persistence.entity.jpa"
})
@EnableJpaRepositories(basePackages = {
    "com.acme.bank.txeventpublisher.infrastructure.persistence.repository"
})
@EnableTransactionManagement
public class TxEventPublisherApplication {

    public static void main(String[] args) {
        SpringApplication.run(TxEventPublisherApplication.class, args);
    }

}
