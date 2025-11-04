package com.acme.bank.payments.domain.idempotence;

public record MoneyTransferOperationRequest(
    String requestId,
    Long senderId,
    Long receiverId,
    Long amount
) {}
