package com.acme.bank.payments.domain.frauddetection;

import com.acme.bank.payments.domain.model.FraudInspectionRequest;
import com.acme.bank.payments.domain.model.FraudInspectionResult;

import java.util.concurrent.CompletableFuture;

public interface FraudDetectionClient {
    CompletableFuture<FraudInspectionResult> inspect(FraudInspectionRequest request);
}
