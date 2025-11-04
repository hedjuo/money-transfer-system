package com.acme.bank.fraud.domain.inspection;

import com.acme.bank.fraud.domain.model.InspectionRequest;
import com.acme.bank.fraud.domain.model.InspectionResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.concurrent.CompletableFuture.completedFuture;

@Component
public class DefaultFraudDetector implements FraudDetector {

    private final List<FraudInspectionStep> fraudFilters;

    private final Random random = ThreadLocalRandom.current();

    public DefaultFraudDetector(List<FraudInspectionStep> filters) {
        this.fraudFilters = filters;
    }

    @Override
    public CompletableFuture<InspectionResponse> inspect(InspectionRequest request) {

        var pass = fraudFilters.stream()
                .map(fraudInspectionStep -> fraudInspectionStep.check(request))
                .map(CompletableFuture::join)
                .toList()
                .stream()
                .reduce(true, (a, b) -> a && b);

        pass = random.nextBoolean();

        var response = pass ?
            InspectionResponse.success()
            : InspectionResponse.failed("Fraud detected");

        return completedFuture(response);
    }
}
