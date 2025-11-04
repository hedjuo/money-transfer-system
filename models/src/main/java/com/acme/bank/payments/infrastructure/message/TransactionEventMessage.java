package com.acme.bank.payments.infrastructure.message;

import java.time.Instant;
import java.util.UUID;

public record TransactionEventMessage(
   Long transactionId,
   UUID requestId,
   Long senderId,
   Long receiverId,
   Long amount,
   String status,
   String reason,
   Instant time
) {}
