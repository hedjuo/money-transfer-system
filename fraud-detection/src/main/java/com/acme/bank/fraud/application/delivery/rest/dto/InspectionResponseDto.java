package com.acme.bank.fraud.application.delivery.rest.dto;

public record InspectionResponseDto(
    Boolean success,
    String message
) {}
