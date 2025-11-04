package com.acme.bank.payments.domain.exception;

import com.acme.bank.payments.domain.vo.AccountId;

public class InsufficientFundsException extends DomainException {
    public InsufficientFundsException(AccountId accountId) {
        super("Insufficient funds. ID: " + accountId.getValue());
    }
}
