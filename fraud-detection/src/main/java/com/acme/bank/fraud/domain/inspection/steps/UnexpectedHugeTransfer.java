package com.acme.bank.fraud.domain.inspection.steps;

import com.acme.bank.fraud.domain.inspection.FraudInspectionStep;
import com.acme.bank.fraud.domain.model.InspectionRequest;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class UnexpectedHugeTransfer implements FraudInspectionStep {
    private final Long limitPerTransaction;

    public UnexpectedHugeTransfer(Long limitPerTransaction) {
        this.limitPerTransaction = limitPerTransaction;
    }


    @Override
    public CompletableFuture<Boolean> check(InspectionRequest request) {
        var amount = request.getAmount();

        return completedFuture(amount < limitPerTransaction);
    }
}
