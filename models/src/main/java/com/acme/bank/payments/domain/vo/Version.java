package com.acme.bank.payments.domain.vo;

import java.util.Objects;

public class Version {
    private final Long value;

    private Version(Long value) {
        this.value = value;
    }

    public static Version of(Long value) {
        return new Version(value);
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version accountId = (Version) o;
        return Objects.equals(value, accountId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Version{" +
                "value=" + value +
                '}';
    }
}
