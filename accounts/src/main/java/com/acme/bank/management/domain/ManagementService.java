package com.acme.bank.management.domain;

import com.acme.bank.payments.domain.model.Account;
import com.acme.bank.payments.domain.model.Client;
import com.acme.bank.payments.domain.vo.AccountId;
import com.acme.bank.payments.domain.vo.ClientId;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ManagementService {
    CompletableFuture<Account> createAccount(ClientId clientId, Long initialBalance);
    CompletableFuture<Optional<Account>> findAccountById(AccountId accountId);
    CompletableFuture<List<Account>> findAccountsByClientId(ClientId clientId);
    CompletableFuture<Client> createClient(String name);
    CompletableFuture<Optional<Client>> findClientById(Long id);
}
