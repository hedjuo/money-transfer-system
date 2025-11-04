package com.acme.bank.management.domain.exception;

import com.acme.bank.payments.domain.vo.AccountId;

public class AccountNotFoundException extends DomainException {
    public AccountNotFoundException(AccountId id) {
        super("Account not found. ID: " + id.getValue());
    }
}
