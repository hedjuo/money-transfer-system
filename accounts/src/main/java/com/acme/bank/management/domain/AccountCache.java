package com.acme.bank.management.domain;

import com.acme.bank.payments.domain.model.Account;
import com.acme.bank.payments.domain.vo.AccountId;
import com.acme.bank.payments.domain.vo.ClientId;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface AccountCache {
    CompletableFuture<Optional<Account>> findById(AccountId id);
    CompletableFuture<List<Account>> findByClientId(ClientId id);
    CompletableFuture<Account> put(Account account);
}
