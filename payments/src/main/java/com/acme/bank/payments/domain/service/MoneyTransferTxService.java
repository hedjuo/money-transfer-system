package com.acme.bank.payments.domain.service;

import com.acme.bank.payments.domain.model.TransferTransaction;
import com.acme.bank.payments.domain.vo.AccountId;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface MoneyTransferTxService {
    CompletableFuture<TransferTransaction> executeTransaction(
        AccountId senderId,
        AccountId receiverId,
        Long amount,
        String requestId
    );
}