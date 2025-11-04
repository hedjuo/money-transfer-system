package com.acme.bank.txeventpublisher.domain;

public interface TransactionEventPublisher {
    void publishNextBatch();
}
