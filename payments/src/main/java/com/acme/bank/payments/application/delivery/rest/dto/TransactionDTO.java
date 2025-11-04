package com.acme.bank.payments.application.delivery.rest.dto;

public record TransactionDTO(
        Long id,
        String requestId,
        Long senderId,
        Long receiverId,
        Long amount,
        Long newSenderBalance,
        Long newReceiverBalance,
        String status
) {}
