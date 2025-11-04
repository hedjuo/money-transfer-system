package com.acme.bank.payments.domain.model;

import java.time.OffsetDateTime;

public record FraudInspectionRequest(
    Long senderId,
    Long receiverId,
    Long amount,
    OffsetDateTime time
) {}
