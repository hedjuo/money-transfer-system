package com.acme.bank.payments.domain.model;

import com.acme.bank.payments.domain.vo.AccountId;
import com.acme.bank.payments.domain.vo.TransactionId;

import java.time.OffsetDateTime;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public class TransferTransactionEvent {
    private final TransactionId id;
    private final UUID requestId;
    private final AccountId senderId;
    private final AccountId receiverId;
    private final Long amount;
    private final Long newSenderBalance;
    private final Long newReceiverBalance;
    private Status status;
    private String reason;
    private OffsetDateTime createdAt;

    public static Builder builder() {
        return new Builder();
    }

    public TransferTransactionEvent(
        TransactionId id,
        UUID requestId,
        AccountId senderId,
        AccountId receiverId,
        Long amount,
        Long newSenderBalance,
        Long newReceiverBalance,
        Status status,
        String reason,
        OffsetDateTime createdAt
    ) {
        this.id = requireNonNull(id, "Transaction id cannot be null");
        this.requestId = requireNonNull(requestId, "Request id cannot be null");
        this.senderId = requireNonNull(senderId, "Sender account id cannot be null");
        this.receiverId = requireNonNull(receiverId, "Receiver account id cannot be null");
        this.amount = requireNonNull(amount, "Amount cannot be null");
        this.status = requireNonNull(status, "Status cannot be null");
        if (status == Status.SUCCESS) {
            this.newSenderBalance = requireNonNull(newSenderBalance, "New sender balance can not be null");
            this.newReceiverBalance = requireNonNull(newReceiverBalance, "New receiver balance can not be null");
        } else {
            this.newSenderBalance = newSenderBalance;
            this.newReceiverBalance = newReceiverBalance;
        }
        this.reason = reason;
        this.createdAt = requireNonNull(createdAt, "Created at cannot be null");
    }

    public TransactionId getId() {
        return id;
    }

    public UUID getRequestId() {
        return requestId;
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

    public Status getStatus() {
        return status;
    }

    public Long getNewSenderBalance() {
        return newSenderBalance;
    }

    public Long getNewReceiverBalance() {
        return newReceiverBalance;
    }

    public String getReason() {
        return reason;
    }

    public void fail(String reason) {
        requireNonNull(reason, "Reason cannot be null");

        this.status = Status.FAILED;
        this.reason = reason;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean isCompleted() {
        return status != Status.CREATED;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public static class Builder {
        private TransactionId id;
        private UUID requestId;
        private AccountId senderId;
        private AccountId receiverId;
        private Long amount;
        private Long newSenderBalance;
        private Long newReceiverBalance;
        private Status status;
        private String reason;
        private OffsetDateTime createdAt;

        public Builder id(TransactionId id) {
            this.id = id;
            return this;
        }

        public Builder requestId(UUID requestId) {
            this.requestId = requestId;
            return this;
        }

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

        public Builder newSenderBalance(Long newSenderBalance) {
            this.newSenderBalance = newSenderBalance;
            return this;
        }

        public Builder newReceiverBalance(Long newReceiverBalance) {
            this.newReceiverBalance = newReceiverBalance;
            return this;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TransferTransactionEvent build() {
            return new TransferTransactionEvent(
                id,
                requestId,
                senderId,
                receiverId,
                amount,
                newSenderBalance,
                newReceiverBalance,
                status,
                reason,
                createdAt
            );
        }
    }

    public enum Status {
        CREATED,
        SUCCESS,
        FAILED
    }
}
