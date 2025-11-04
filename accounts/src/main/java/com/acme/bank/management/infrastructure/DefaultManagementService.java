package com.acme.bank.management.infrastructure;

import com.acme.bank.management.domain.AccountCache;
import com.acme.bank.management.domain.AccountRepository;
import com.acme.bank.management.domain.ClientRepository;
import com.acme.bank.management.domain.ManagementService;
import com.acme.bank.payments.domain.model.Account;
import com.acme.bank.payments.domain.model.Client;
import com.acme.bank.payments.domain.vo.AccountId;
import com.acme.bank.payments.domain.vo.ClientId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

@Component
public class DefaultManagementService implements ManagementService {
    private final AccountCache cache;
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;

    public DefaultManagementService(
        AccountCache cache,
        AccountRepository accountRepository,
        ClientRepository clientRepository
    ) {
        this.cache = cache;
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public CompletableFuture<Account> createAccount(ClientId clientId, Long initialBalance) {
        var account = Account.builder()
                .clientId(clientId)
                .balance(initialBalance)
                .build();
        return accountRepository.save(account);
    }

    @Override
    public CompletableFuture<Optional<Account>> findAccountById(AccountId accountId) {
        CompletableFuture<Optional<Account>> accountFuture = cache.findById(accountId);

        return accountFuture.thenCompose(accountOptional -> {
            return accountOptional
                .map(cachedAccount -> {
                    var cachedAccOptional = Optional.of(cachedAccount);
                    return completedFuture(cachedAccOptional);
                })
                .orElseGet(() -> {
                    var loadedAccFuture = accountRepository.findById(accountId);

                    return loadedAccFuture.thenCompose(loadedAccOptional -> {
                        if (loadedAccOptional.isEmpty()) {
                            return completedFuture(Optional.empty());
                        } else {
                            Account loadedAcc = loadedAccOptional.get();
                            return cache.put(loadedAcc).thenApply(Optional::of);
                        }
                    });
                });
        });
    }

    @Override
    public CompletableFuture<List<Account>> findAccountsByClientId(ClientId clientId) {
        return cache.findByClientId(clientId);
    }

    @Override
    public CompletableFuture<Client> createClient(String name) {
        var client = Client.builder()
                .name(name)
                .build();

        return clientRepository.save(client);
    }

    @Override
    public CompletableFuture<Optional<Client>> findClientById(Long id) {
        return clientRepository.findById(ClientId.of(id));
    }

    private CompletableFuture<Account> toAccount(Account account) {
        return null;
    }
}
