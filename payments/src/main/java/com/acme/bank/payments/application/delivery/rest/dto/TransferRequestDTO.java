package com.acme.bank.payments.application.delivery.rest.dto;

public record TransferRequestDTO(
    String requestId,
    Long senderId,
    Long receiverId,
    Long amount
){}
