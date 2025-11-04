package com.acme.bank.management.dto;

public record AccountDTO(
    Long id,
    Long clientId,
    Long balance,
    Long version
) {}
