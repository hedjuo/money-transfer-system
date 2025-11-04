package com.acme.bank.payments.domain.repository;

import com.acme.bank.payments.domain.model.TransferTransaction;

import java.util.concurrent.CompletableFuture;

public interface TransferTransactionRepository {
    CompletableFuture<TransferTransaction> save(TransferTransaction transaction);
}
