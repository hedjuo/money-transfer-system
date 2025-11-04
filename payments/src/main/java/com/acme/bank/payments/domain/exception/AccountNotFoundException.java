package com.acme.bank.payments.domain.exception;

public class AccountNotFoundException extends DomainException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
