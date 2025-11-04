package com.acme.bank.payments.domain.model;

import com.acme.bank.payments.domain.vo.AccountId;
import com.acme.bank.payments.domain.vo.ClientId;
import com.acme.bank.payments.domain.vo.Version;

import static java.util.Objects.requireNonNull;

public class Account {
    private final AccountId id;
    private final ClientId clientId;
    private final Long balance;
    private final Version version;

    private Account(AccountId id, ClientId clientId, Long balance, Version version) {
        this.id = id;
        this.clientId = requireNonNull(clientId, "client id can not be null");
        this.balance = requireNonNull(balance, "balance can not be null");
        this.version = version;

        if (balance < 0) {
            throw new IllegalArgumentException("balance can not be negative");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public AccountId getId() {
        return id;
    }

    public ClientId getClientId() {
        return clientId;
    }

    public void setBalance(Long balance) {
        if (balance < 0) {
            throw new RuntimeException("Balance can not be negative");
        }
    }

    public Long getBalance() {
        return balance;
    }

    public Version getVersion() {
        return version;
    }

    public static class Builder {
        private AccountId id;
        private ClientId clientId;
        private Long balance;
        private Version version;

        public Builder id(AccountId id) {
            this.id = id;
            return this;
        }

        public Builder clientId(ClientId clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder balance(Long balance) {
            this.balance = balance;
            return this;
        }

        public Builder version(Version version) {
            this.version = version;
            return this;
        }

        public Account build() {
            return new Account(id, clientId, balance, version);
        }
    }
}
