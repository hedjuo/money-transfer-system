package com.acme.bank.payments.infrastructure.persistence.repository;

import com.acme.bank.payments.domain.model.Account;
import com.acme.bank.payments.domain.repository.AccountRepository;
import com.acme.bank.payments.domain.vo.AccountId;
import com.acme.bank.payments.domain.vo.ClientId;
import com.acme.bank.payments.domain.vo.Version;
import com.acme.bank.payments.infrastructure.persistence.entity.relational.AccountEntity;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class ReactiveAccountRepository implements AccountRepository {
    private final DatabaseAccountRepository delegate;

    public ReactiveAccountRepository(DatabaseAccountRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public CompletableFuture<Account> save(Account account) {
        return delegate
                .save(toEntity(account))
                .map(this::toDomain)
                .toFuture();
    }

    @Override
    public CompletableFuture<Account> findById(AccountId id) {
        return delegate.findById(id.getValue())
                .map(this::toDomain)
                .toFuture();
    }

    private AccountEntity toEntity(Account account) {
        var entity = new AccountEntity();

        entity.setId(account.getId().getValue());
        entity.setClientId(account.getClientId().getValue());
        entity.setBalance(account.getBalance());
        entity.setVersion(account.getVersion().getValue());

        return entity;
    }

    private Account toDomain(AccountEntity accountEntity) {
        return Account.builder()
                .id(AccountId.of(accountEntity.getId()))
                .clientId(ClientId.of(accountEntity.getClientId()))
                .balance(accountEntity.getBalance())
                .version(Version.of(accountEntity.getVersion()))
                .build();
    }
}
