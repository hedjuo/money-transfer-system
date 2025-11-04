package com.acme.bank.txeventpublisher.infrastructure.persistence.repository;

import com.acme.bank.payments.infrastructure.persistence.entity.jpa.TransactionEventEntity;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaTransactionEventRepository extends ListCrudRepository<TransactionEventEntity, Long> {
    @NativeQuery("SELECT * FROM transaction_event e ORDER BY e.id ASC FOR UPDATE SKIP LOCKED LIMIT :n")
    List<TransactionEventEntity> fetchNRecords(@Param("n") int n);
}
