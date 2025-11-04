package com.acme.bank.payments.domain.repository;

import com.acme.bank.payments.domain.model.Account;
import com.acme.bank.payments.domain.vo.AccountId;

import java.util.concurrent.CompletableFuture;

public interface AccountRepository {
    CompletableFuture<Account> save(Account account);
    CompletableFuture<Account> findById(AccountId id);
}
