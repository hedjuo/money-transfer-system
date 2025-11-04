package com.acme.bank.migrations.infrastructure.persistence.repository;


import com.acme.bank.payments.infrastructure.persistence.entity.jpa.TransactionEventEntity;
import org.springframework.data.repository.CrudRepository;


public interface TransactionEventRepository extends CrudRepository<TransactionEventEntity, Long> {}
