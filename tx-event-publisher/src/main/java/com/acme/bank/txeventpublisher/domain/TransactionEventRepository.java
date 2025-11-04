package com.acme.bank.txeventpublisher.domain;

import com.acme.bank.payments.domain.model.TransferTransactionEvent;

import java.util.List;

public interface TransactionEventRepository {
    List<TransferTransactionEvent> fetchTransactionEvents(Integer count);
    void deleteById(Long id);
}
