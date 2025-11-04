package com.acme.bank.fraud.domain.model;

import com.acme.bank.fraud.domain.vo.AccountId;

import java.util.Objects;

public class InspectionRequest {
    private final AccountId senderId;
    private final AccountId receiverId;
    private final Long amount;

    private InspectionRequest(AccountId senderId, AccountId receiverId, Long amount) {
        this.senderId = Objects.requireNonNull(senderId, "senderId can not be null");
        this.receiverId = Objects.requireNonNull(receiverId, "receiverId can not be null");
        this.amount = Objects.requireNonNull(amount, "amount can not be null");
    }

    public AccountId getSenderId() {
        return senderId;
    }

    public AccountId getReceiverId() {
        return receiverId;
    }

    public Long getAmount() {
        return amount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AccountId senderId;
        private AccountId receiverId;
        private Long amount;

        public Builder senderId(AccountId senderId) {
            this.senderId = senderId;
            return this;
        }

        public Builder receiverId(AccountId receiverId) {
            this.receiverId = receiverId;
            return this;
        }

        public Builder amount(Long amount) {
            this.amount = amount;
            return this;
        }

        public InspectionRequest build() {
            return new InspectionRequest(senderId, receiverId, amount);
        }
    }
}
