package com.acme.bank.management.dto;

public record CreateAccountRequest(
    Long clientId,
    Long initialBalance
) {}
