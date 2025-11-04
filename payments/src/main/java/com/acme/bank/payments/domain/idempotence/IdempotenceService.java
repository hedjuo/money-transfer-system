package com.acme.bank.payments.domain.idempotence;

import com.acme.bank.payments.domain.model.TransferTransaction;

import java.util.concurrent.CompletableFuture;

public interface IdempotenceService {
    CompletableFuture<TransferTransaction> execute(String idempotencyKey, IdempotentOperation idempotentOperation);
}
