package com.acme.bank.management.infrastructure.service;

import com.acme.bank.management.application.service.AccountCacheRefresher;
import com.acme.bank.management.domain.AccountCache;
import com.acme.bank.management.domain.AccountRepository;
import com.acme.bank.payments.domain.vo.AccountId;
import org.springframework.stereotype.Component;

@Component
public class DefaultAccountCacheRefresher implements AccountCacheRefresher {
    private final AccountRepository accountRepository;
    private final AccountCache cache;

    public DefaultAccountCacheRefresher(
            AccountRepository accountRepository,
            AccountCache cache
    ) {
        this.accountRepository = accountRepository;
        this.cache = cache;
    }

    @Override
    public void loadAccountIntoCache(AccountId accountId) {
        accountRepository
            .findById(accountId)
            .thenApply(accountOptional -> {
                accountOptional.ifPresent(cache::put);
                return null;
            });
    }
}
