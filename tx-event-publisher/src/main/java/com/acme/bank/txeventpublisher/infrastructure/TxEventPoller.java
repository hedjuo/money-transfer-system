package com.acme.bank.txeventpublisher.infrastructure;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TxEventPoller {
    private static final Logger log = LoggerFactory.getLogger(TxEventPoller.class);

    private final DefaultTransactionEventPublisher eventPublisher;

    public TxEventPoller(DefaultTransactionEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedRate = 100)
    @SchedulerLock(name = "pollTxEventLock", lockAtMostFor = "PT5M")
    public void poll() {
        try {
            eventPublisher.publishNextBatch();
        } catch (Exception e) {
            log.error("Error while publishing next batch", e);
        }
    }
}
