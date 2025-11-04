package com.acme.bank.management.application.service;

import com.acme.bank.payments.domain.vo.AccountId;

public interface AccountCacheRefresher {
    void loadAccountIntoCache(AccountId accountId);
}
