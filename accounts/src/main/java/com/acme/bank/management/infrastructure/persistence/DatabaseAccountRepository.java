package com.acme.bank.management.infrastructure.persistence;

import com.acme.bank.management.domain.AccountRepository;
import com.acme.bank.payments.domain.model.Account;
import com.acme.bank.payments.domain.vo.AccountId;
import com.acme.bank.payments.domain.vo.ClientId;
import com.acme.bank.payments.domain.vo.Version;
import com.acme.bank.payments.infrastructure.persistence.entity.relational.AccountEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class DatabaseAccountRepository implements AccountRepository {

    private final ReactiveAccountRepository delegate;

    public DatabaseAccountRepository(ReactiveAccountRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public CompletableFuture<Account> save(Account account) {
        var entity = toEntity(account);

        return delegate.save(entity)
                .map(this::toAccount)
                .toFuture();
    }

    @Override
    public CompletableFuture<Optional<Account>> findById(AccountId id) {
        return delegate.findById(id.getValue())
                .map(this::toAccountOptional)
                .defaultIfEmpty(Optional.empty())
                .toFuture();
    }

    private Optional<Account> toAccountOptional(AccountEntity entity) {
        return Optional.of(toAccount(entity));
    }

    private Account toAccount(AccountEntity entity) {
        return Account.builder()
                .id(AccountId.of(entity.getId()))
                .clientId(ClientId.of(entity.getClientId()))
                .balance(entity.getBalance())
                .version(Version.of(entity.getVersion()))
                .build();
    }

    private AccountEntity toEntity(Account account) {
        var entitity = new AccountEntity();
        entitity.setClientId(account.getClientId().getValue());
        entitity.setBalance(account.getBalance());
        return entitity;
    }
}
