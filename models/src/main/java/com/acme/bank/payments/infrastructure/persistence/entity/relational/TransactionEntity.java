package com.acme.bank.payments.infrastructure.persistence.entity.relational;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("transaction")
public class TransactionEntity {
    @Id
    private Long id;

    private UUID requestId;

    private Long senderId;

    private Long receiverId;

    private Long amount;

    private Long newSenderBalance;

    private Long newReceiverBalance;

    private String status;

    private String reason;

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Long getAmount() {
        return amount;
    }

    public Long getNewSenderBalance() {
        return newSenderBalance;
    }

    public void setNewSenderBalance(Long newSenderBalance) {
        this.newSenderBalance = newSenderBalance;
    }

    public Long getNewReceiverBalance() {
        return newReceiverBalance;
    }

    public void setNewReceiverBalance(Long newReceiverBalance) {
        this.newReceiverBalance = newReceiverBalance;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
