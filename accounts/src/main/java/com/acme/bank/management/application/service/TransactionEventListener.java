package com.acme.bank.management.application.service;

import com.acme.bank.payments.domain.vo.AccountId;
import com.acme.bank.payments.infrastructure.message.TransactionEventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

import java.time.Duration;

@Service
public class TransactionEventListener {
    private static final Logger log = LoggerFactory.getLogger(TransactionEventListener.class);

    private final KafkaReceiver<String, TransactionEventMessage> kafkaReceiver;
    private final AccountCacheRefresher cacheRefresher;
    private Disposable subscription;

    public TransactionEventListener(
            KafkaReceiver<String, TransactionEventMessage> kafkaReceiver,
            AccountCacheRefresher cacheRefresher
    ) {
        this.kafkaReceiver = kafkaReceiver;
        this.cacheRefresher = cacheRefresher;
    }

    @EventListener
    public void startConsuming(ApplicationReadyEvent are) {
        subscription = kafkaReceiver.receive()
                .doOnNext(record -> log.debug("Received record: partition={}, offset={}",
                        record.partition(), record.offset()))
                // Process messages with concurrency control
                .flatMap(record -> processMessage(record)
                                .timeout(Duration.ofSeconds(30)) // Timeout per message
                                .retry(2), // Retry failed messages
                        10) // Process up to 10 messages concurrently
                // Commit offsets in batches for better performance
                .bufferTimeout(100, Duration.ofSeconds(5))
                .doOnNext(batch -> log.debug("Processed batch of {} messages", batch.size()))
                .subscribe(
                        batch -> log.debug("Batch committed"),
                        error -> log.error("Error in Kafka consumer", error),
                        () -> log.info("Kafka consumer completed")
                );
    }


    public void destroy() throws Exception {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            log.info("Kafka consumer stopped");
        }
    }

    private Mono<ReceiverRecord<String, TransactionEventMessage>> processMessage(ReceiverRecord<String, TransactionEventMessage> record) {
        return Mono.fromCallable(() -> {
            var transactionEvent = record.value();

            if (!"success".equalsIgnoreCase(transactionEvent.status())) {
                return record;
            }
            cacheRefresher.loadAccountIntoCache(AccountId.of(transactionEvent.senderId()));
            cacheRefresher.loadAccountIntoCache(AccountId.of(transactionEvent.receiverId()));

            return record;
        })
        .doOnSuccess(r -> r.receiverOffset().acknowledge())
        .doOnError(error ->
            log.error("Failed to process message at offset {}: {}", record.offset(), error.getMessage())
        );
    }
}
