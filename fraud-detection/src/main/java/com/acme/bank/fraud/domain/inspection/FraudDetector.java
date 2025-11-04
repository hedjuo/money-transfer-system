package com.acme.bank.fraud.domain.inspection;

import com.acme.bank.fraud.domain.model.InspectionRequest;
import com.acme.bank.fraud.domain.model.InspectionResponse;

import java.util.concurrent.CompletableFuture;

public interface FraudDetector {
    CompletableFuture<InspectionResponse> inspect(InspectionRequest request);
}
