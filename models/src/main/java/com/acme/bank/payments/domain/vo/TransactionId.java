package com.acme.bank.payments.domain.vo;

import java.util.Objects;

public class TransactionId {
    private final Long value;

    public static TransactionId of(Long value) {
        return new TransactionId(value);
    }

    private TransactionId(Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionId accountId = (TransactionId) o;
        return Objects.equals(value, accountId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "TransactionId{" +
                "value=" + value +
                '}';
    }
}
