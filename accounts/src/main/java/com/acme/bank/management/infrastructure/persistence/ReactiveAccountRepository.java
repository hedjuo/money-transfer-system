package com.acme.bank.management.infrastructure.persistence;


import com.acme.bank.payments.infrastructure.persistence.entity.relational.AccountEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Component;


@Component
public interface ReactiveAccountRepository extends R2dbcRepository<AccountEntity, Long> {
}
