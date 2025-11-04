package com.acme.bank.txeventpublisher.infrastructure;

import com.acme.bank.payments.domain.model.TransferTransactionEvent;
import com.acme.bank.payments.domain.vo.TransactionId;
import com.acme.bank.txeventpublisher.domain.TransactionEventPublisher;
import com.acme.bank.txeventpublisher.domain.TransactionEventRepository;
import com.acme.bank.txeventpublisher.domain.message.TransactionEventQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;

@Service
public class DefaultTransactionEventPublisher implements TransactionEventPublisher {
    private final Logger log = LoggerFactory.getLogger(DefaultTransactionEventPublisher.class);
    private final TransactionEventRepository txEventRepository;
    private final TransactionEventQueue txEventQueue;
    private final Integer eventBatchSize;

    public DefaultTransactionEventPublisher(
            TransactionEventRepository txEventRepository,
            TransactionEventQueue txEventQueue,
            @Value("${app.worker.batch-size}") Integer eventBatchSize
    ) {
        this.txEventRepository = txEventRepository;
        this.txEventQueue = txEventQueue;
        this.eventBatchSize = eventBatchSize;
    }

    @Transactional
    public void publishNextBatch() {
        var toDelete = new LinkedList<TransferTransactionEvent>();
        var txEvents = txEventRepository.fetchTransactionEvents(eventBatchSize);

        log.debug("Found {} transaction events", txEvents.size());

        for (var event : txEvents) {
            try {
                txEventQueue.push(event);
                toDelete.add(event);
            } catch (Exception e) {
                log.error("Error occurred while processing transaction event: {}" , event, e);
            }
        }

        toDelete.stream()
                .map(TransferTransactionEvent::getId)
                .map(TransactionId::getValue)
                .forEach(txEventRepository::deleteById);

        log.debug("Published batch of {} transaction events", toDelete.size());
    }
}
