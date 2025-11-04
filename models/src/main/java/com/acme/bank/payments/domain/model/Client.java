package com.acme.bank.payments.domain.model;

import com.acme.bank.payments.domain.vo.ClientId;

import java.util.List;

public class Client {
    private final ClientId id;
    private final String name;
    private final List<Account> accounts;

    public static Builder builder() {
        return new Builder();
    }

    public Client(ClientId id, String name, List<Account> accounts) {
        this.id = id;
        this.name = name;
        this.accounts = accounts;
    }

    public ClientId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public static class Builder {
        private ClientId id;
        private String name;
        private List<Account> accounts;

        public Builder id(ClientId id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder accounts(List<Account> accounts) {
            this.accounts = accounts;
            return this;
        }

        public Client build() {
            return new Client(id, name, accounts);
        }
    }
}
