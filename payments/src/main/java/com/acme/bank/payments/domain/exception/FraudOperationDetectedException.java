package com.acme.bank.payments.domain.exception;

public class FraudOperationDetectedException extends DomainException {
    public FraudOperationDetectedException() {
        super("Fraud operation detected.");
    }
}
