package com.acme.bank.payments.domain.vo;

import java.util.Objects;

public class ClientId {
    private final Long value;

    public static ClientId of(Long value) {
        return new ClientId(value);
    }

    private ClientId(Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientId accountId = (ClientId) o;
        return Objects.equals(value, accountId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "ClientId{" +
                "value=" + value +
                '}';
    }
}
