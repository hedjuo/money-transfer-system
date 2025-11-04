package com.acme.bank.migrations.infrastructure.persistence.repository;


import com.acme.bank.payments.infrastructure.persistence.entity.jpa.TransactionEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface TransactionRepository extends ListCrudRepository<TransactionEntity, Long> {}
