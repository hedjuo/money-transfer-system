package com.acme.bank.payments.domain.idempotence;

import com.acme.bank.payments.domain.model.TransferTransaction;

import java.util.concurrent.CompletableFuture;

public interface IdempotentOperation {
    CompletableFuture<TransferTransaction> execute();
}
