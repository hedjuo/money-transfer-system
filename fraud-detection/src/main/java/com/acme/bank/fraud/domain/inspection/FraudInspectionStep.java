package com.acme.bank.fraud.domain.inspection;

import com.acme.bank.fraud.domain.model.InspectionRequest;

import java.util.concurrent.CompletableFuture;

public interface FraudInspectionStep {
    CompletableFuture<Boolean> check(InspectionRequest request);
}
