package com.acme.bank.fraud.application.delivery.rest.dto;

import java.time.OffsetDateTime;

public record InspectionRequest(
    Long senderId,
    Long receiverId,
    Long amount,
    OffsetDateTime time
) {}
