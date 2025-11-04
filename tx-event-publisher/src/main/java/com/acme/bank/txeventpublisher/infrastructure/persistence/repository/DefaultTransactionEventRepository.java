package com.acme.bank.txeventpublisher.infrastructure.persistence.repository;

import com.acme.bank.payments.domain.model.TransferTransactionEvent.Status;
import com.acme.bank.payments.domain.model.TransferTransactionEvent;
import com.acme.bank.payments.domain.vo.AccountId;
import com.acme.bank.payments.domain.vo.TransactionId;
import com.acme.bank.payments.infrastructure.persistence.entity.jpa.TransactionEventEntity;
import com.acme.bank.txeventpublisher.domain.TransactionEventRepository;


import java.util.List;

public class DefaultTransactionEventRepository implements TransactionEventRepository {
    private final JpaTransactionEventRepository delegate;

    public DefaultTransactionEventRepository(JpaTransactionEventRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<TransferTransactionEvent> fetchTransactionEvents(Integer count) {
        return delegate.fetchNRecords(count)
                .stream()
                .map(this::toTransferTransactionEvent)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        delegate.deleteById(id);
    }

    private TransferTransactionEvent toTransferTransactionEvent(TransactionEventEntity entity) {
        return TransferTransactionEvent.builder()
                .id(TransactionId.of(entity.getId()))
                .requestId(entity.getRequestId())
                .senderId(AccountId.of(entity.getSenderId()))
                .receiverId(AccountId.of(entity.getReceiverId()))
                .amount(entity.getAmount())
                .newSenderBalance(entity.getNewSenderBalance())
                .newReceiverBalance(entity.getNewReceiverBalance())
                .status(Status.valueOf(entity.getStatus()))
                .reason(entity.getReason())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
