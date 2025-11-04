package com.acme.bank.payments.infrastructure.frauddetection;

import com.acme.bank.payments.domain.frauddetection.FraudDetectionClient;
import com.acme.bank.payments.domain.frauddetection.FraudDetectionService;
import com.acme.bank.payments.domain.model.FraudInspectionRequest;
import com.acme.bank.payments.domain.model.FraudInspectionResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class DefaultFraudDetectionService implements FraudDetectionService {

    private final FraudDetectionClient client;

    public DefaultFraudDetectionService(FraudDetectionClient client) {
        this.client = client;
    }

    @Override
    public CompletableFuture<FraudInspectionResult> inspect(FraudInspectionRequest request) {
        return client.inspect(request);
    }
}
