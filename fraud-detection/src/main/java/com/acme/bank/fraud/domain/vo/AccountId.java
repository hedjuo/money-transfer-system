package com.acme.bank.fraud.domain.vo;

import java.util.Objects;

public class AccountId {
    private final Long value;

    private AccountId(Long value) {
        this.value = value;
    }

    public static AccountId of(Long value) {
        return new AccountId(value);
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountId accountId = (AccountId) o;
        return Objects.equals(value, accountId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "AccountId{" +
                "value=" + value +
                '}';
    }
}
