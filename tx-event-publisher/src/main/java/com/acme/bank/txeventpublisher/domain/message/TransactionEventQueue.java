package com.acme.bank.txeventpublisher.domain.message;

import com.acme.bank.payments.domain.model.TransferTransactionEvent;

public interface TransactionEventQueue {
    void push(TransferTransactionEvent event);
}
