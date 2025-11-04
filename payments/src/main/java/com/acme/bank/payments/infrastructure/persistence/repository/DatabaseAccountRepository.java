package com.acme.bank.payments.infrastructure.persistence.repository;

import com.acme.bank.payments.infrastructure.persistence.entity.relational.AccountEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Component;

@Component
public interface DatabaseAccountRepository extends R2dbcRepository<AccountEntity, Long> {}
