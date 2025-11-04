package com.acme.bank.payments.domain.idempotence;

import com.acme.bank.payments.domain.model.TransferTransaction;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class MoneyTransferOperation implements IdempotentOperation {
    private final Function<MoneyTransferOperationRequest, CompletableFuture<TransferTransaction>> function;
    private final MoneyTransferOperationRequest request;

    public MoneyTransferOperation(
            MoneyTransferOperationRequest request,
            Function<MoneyTransferOperationRequest, CompletableFuture<TransferTransaction>> function
    ) {
        this.function = function;
        this.request = request;
    }

    @Override
    public CompletableFuture<TransferTransaction> execute() {
        return function.apply(request);
    }
}
