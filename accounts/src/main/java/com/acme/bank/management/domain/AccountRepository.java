package com.acme.bank.management.domain;

import com.acme.bank.payments.domain.model.Account;
import com.acme.bank.payments.domain.vo.AccountId;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface AccountRepository {
    CompletableFuture<Account> save(Account account);
    CompletableFuture<Optional<Account>> findById(AccountId id);
}
