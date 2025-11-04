package com.acme.bank.payments.domain.exception;

public class VersionMismatchException extends DomainException {
    public VersionMismatchException(String message) {
        super(message);
    }
}
