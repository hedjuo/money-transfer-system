package com.acme.bank.payments.domain.vo;

import java.util.Objects;
import java.util.UUID;

public class RequestId {
    private final UUID value;

    private RequestId(UUID value) {
        this.value = value;
    }

    public static RequestId of(UUID value) {
        return new RequestId(value);
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestId accountId = (RequestId) o;
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
