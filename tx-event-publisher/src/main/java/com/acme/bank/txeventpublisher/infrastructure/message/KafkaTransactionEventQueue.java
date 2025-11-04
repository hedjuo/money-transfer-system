package com.acme.bank.txeventpublisher.infrastructure.message;

import com.acme.bank.payments.domain.model.TransferTransactionEvent;
import com.acme.bank.payments.infrastructure.message.TransactionEventMessage;
import com.acme.bank.txeventpublisher.domain.message.TransactionEventQueue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaTransactionEventQueue implements TransactionEventQueue {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaTransactionEventQueue.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kafka.transaction-events-topic}")
    private String topicName;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaTransactionEventQueue(
        KafkaTemplate<String, String> kafkaTemplate
    ) {
        objectMapper.registerModule(new JavaTimeModule());
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void push(TransferTransactionEvent event) {
        var message = this.toTransactionEventMessage(event);

        try {
            kafkaTemplate.send(topicName, objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            LOG.error("Unable serialize transaction event", e);
            throw new RuntimeException(e);
        }
    }

    private TransactionEventMessage toTransactionEventMessage(TransferTransactionEvent e) {
        return new TransactionEventMessage(
            e.getId().getValue(),
            e.getRequestId(),
            e.getSenderId().getValue(),
            e.getReceiverId().getValue(),
            e.getAmount(),
            e.getStatus().name(),
            e.getReason(),
            e.getCreatedAt().toInstant()
        );
    }
}
