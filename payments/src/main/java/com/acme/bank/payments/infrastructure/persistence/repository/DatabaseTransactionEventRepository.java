package com.acme.bank.payments.infrastructure.persistence.repository;

import com.acme.bank.payments.infrastructure.persistence.entity.relational.TransactionEventEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Component;

@Component
public interface DatabaseTransactionEventRepository extends R2dbcRepository<TransactionEventEntity, Long> {}
