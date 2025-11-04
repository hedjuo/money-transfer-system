package com.acme.bank.payments.infrastructure.service;

import com.acme.bank.payments.domain.exception.AccountNotFoundException;
import com.acme.bank.payments.domain.exception.DomainException;
import com.acme.bank.payments.domain.exception.FraudOperationDetectedException;
import com.acme.bank.payments.domain.exception.InsufficientFundsException;
import com.acme.bank.payments.domain.frauddetection.FraudDetectionService;
import com.acme.bank.payments.domain.model.FraudInspectionRequest;
import com.acme.bank.payments.domain.model.FraudInspectionResult;
import com.acme.bank.payments.domain.model.TransferTransaction;
import com.acme.bank.payments.domain.service.MoneyTransferTxService;
import com.acme.bank.payments.domain.vo.AccountId;
import com.acme.bank.payments.domain.vo.TransactionId;
import com.acme.bank.payments.infrastructure.persistence.entity.relational.AccountEntity;
import com.acme.bank.payments.infrastructure.persistence.entity.relational.TransactionEntity;
import com.acme.bank.payments.infrastructure.persistence.entity.relational.TransactionEventEntity;
import com.acme.bank.payments.infrastructure.persistence.repository.DatabaseAccountRepository;
import com.acme.bank.payments.infrastructure.persistence.repository.DatabaseTransactionEventRepository;
import com.acme.bank.payments.infrastructure.persistence.repository.DatabaseTransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.r2dbc.UncategorizedR2dbcException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofMinutes;

@Component
public class ReactiveMoneyTransferTxService implements MoneyTransferTxService {
    private static final Logger log = LoggerFactory.getLogger(ReactiveMoneyTransferTxService.class);
    private final DatabaseAccountRepository accountRepository;
    private final DatabaseTransactionRepository transactionRepository;
    private final DatabaseTransactionEventRepository transactionEventRepository;
    private final TransactionalOperator txOperator;
    private final FraudDetectionService fraudDetectionService;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public ReactiveMoneyTransferTxService(
        DatabaseAccountRepository accountRepository,
        DatabaseTransactionRepository transactionRepository,
        DatabaseTransactionEventRepository transactionEventRepository,
        TransactionalOperator txOperator,
        FraudDetectionService fraudDetectionService,
        ObjectMapper objectMapper,
        ReactiveStringRedisTemplate redisTemplate
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionEventRepository = transactionEventRepository;
        this.txOperator = txOperator;
        this.fraudDetectionService = fraudDetectionService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public CompletableFuture<TransferTransaction> executeTransaction(
            AccountId senderAccountId,
            AccountId receiverAccountId,
            Long amount,
            String requestId
    ) {
        var ctx = new Context();
        ctx.setSenderAccountId(senderAccountId.getValue());
        ctx.setReceiverAccountId(receiverAccountId.getValue());
        ctx.setRequestId(requestId);
        ctx.setAmount(amount);

        return inspectRequest(ctx)
                 .flatMap(inspectionResult -> {
                     var executionFlow = buildExecutionFlow(ctx, inspectionResult);
                     return txOperator.transactional(executionFlow)
                                      .retryWhen(this.retrySpec());
                 }).toFuture();
    }

    private Mono<FraudInspectionResult> inspectRequest(Context ctx) {
        var senderId = ctx.getSenderAccountId();
        var receiverId = ctx.getReceiverAccountId();
        var amount = ctx.getAmount();
        var timestamp = OffsetDateTime.now();

        var fraudInspectionRequest = new FraudInspectionRequest(senderId, receiverId, amount, timestamp);
        var fraudInspectionResult = fraudDetectionService.inspect(fraudInspectionRequest);

        return Mono.fromFuture(fraudInspectionResult);
    }

    private Mono<TransferTransaction> buildExecutionFlow(Context ctx, FraudInspectionResult inspectionResult) {
        return Mono.defer(() -> {
            if (!inspectionResult.getSuccess()) {
                return Mono.error(new FraudOperationDetectedException());
            }
            return Mono.just(ctx);
        })
        .zipWith(
            loadAccounts(ctx),
            setAccountsToContext()
        )
        .flatMap(this::moveFunds)
        .map(this::toTransferTransaction);
//        .onErrorResume(onError(ctx));
    }

    private Function<Throwable, Mono<TransferTransaction>> onError(Context ctx) {
        return t -> {
            if (t instanceof DomainException) {
                var transaction = toTransferTransaction(failedTransactionEntity(ctx, t.getMessage()));
                return Mono.just(transaction);
            } else {
                return Mono.error(t);
            }
        };
    }

    private Mono<Tuple2<AccountEntity, AccountEntity>> loadAccounts(Context ctx) {
        var senderId = ctx.getSenderAccountId();
        var receiverId = ctx.getReceiverAccountId();

        return Mono.zip(
                accountRepository
                    .findById(senderId)
                    .switchIfEmpty(accountNotFoundError(senderId)),
                accountRepository
                    .findById(receiverId)
                    .switchIfEmpty(accountNotFoundError(receiverId))
        );
    }

    private static BiFunction<Context, Tuple2<AccountEntity, AccountEntity>, Context> setAccountsToContext() {
        return (context, tuple) -> {
            context.setSenderEntity(tuple.getT1());
            context.setReceiverEntity(tuple.getT2());
            return context;
        };
    }

    private Mono<TransactionEntity> moveFunds(Context ctx) {
        var senderEntity = ctx.getSenderEntity();
        var receiverEntity = ctx.getReceiverEntity();
        var amount = ctx.getAmount();
        var senderId = AccountId.of(senderEntity.getId());
        var receiverId = AccountId.of(receiverEntity.getId());
        var senderBalance = senderEntity.getBalance();

        var txEntity = transactionEntity(senderId, receiverId, amount, ctx.getRequestId());

        if (senderBalance < amount) {
            return Mono.error(new InsufficientFundsException(senderId));
        }

        var newSenderBalance = senderEntity.getBalance() - amount;
        senderEntity.setBalance(newSenderBalance);
        txEntity.setNewSenderBalance(newSenderBalance);

        var newReceiverBalance = receiverEntity.getBalance() + amount;
        receiverEntity.setBalance(newReceiverBalance);
        txEntity.setNewReceiverBalance(newReceiverBalance);

        return accountRepository.save(senderEntity)
                .then(accountRepository.save(receiverEntity))
                .then(saveTransactionAndPublishEvent(txEntity));
    }

    private TransactionEntity failedTransactionEntity(Context ctx, String reason) {
        var entity = new TransactionEntity();

        entity.setRequestId(UUID.fromString(ctx.getRequestId()));
        entity.setSenderId(ctx.getSenderAccountId());
        entity.setReceiverId(ctx.getReceiverAccountId());
        entity.setAmount(ctx.getAmount());
        entity.setStatus(TransferTransaction.Status.FAILED.name());
        entity.setReason(reason);

        return entity;
    }

    private Mono<AccountEntity> accountNotFoundError(Long accountId) {
        return Mono.error(new AccountNotFoundException("Account not found. ID: " + accountId));
    }

    private Mono<TransactionEntity> saveTransactionAndPublishEvent(TransactionEntity transactionEntity) {
        var txEventEntity = transactionEventEntity(transactionEntity);

        return transactionRepository.save(transactionEntity)
                .then(transactionEventRepository.save(txEventEntity))
                .then(Mono.just(transactionEntity));
    }

    private Retry retrySpec() {
        return
            Retry.backoff(Integer.MAX_VALUE, ofMillis(3))
                .multiplier(2)
                .jitter(0.25)
                .filter(this::optimisticLockOrTransientFailure)
                .doBeforeRetry(t -> log.trace("{} Retrying transaction. Attempt {}", t.failure().getMessage(), t.totalRetries() + 1));
    }

    private boolean optimisticLockOrTransientFailure(Throwable t) {
        var result =
            t instanceof TransientDataAccessException
            || t instanceof UncategorizedR2dbcException;

        return result;
    }

    private TransferTransaction toTransferTransaction(String json) {
        TransactionEntity entity = null;
        try {
            entity = objectMapper.readValue(json, TransactionEntity.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return toTransferTransaction(entity);
    }

    private TransferTransaction toTransferTransaction(TransactionEntity entity) {
        return TransferTransaction.builder()
                .id(TransactionId.of(entity.getId()))
                .requestId(entity.getRequestId())
                .senderId(AccountId.of(entity.getSenderId()))
                .receiverId(AccountId.of(entity.getReceiverId()))
                .amount(entity.getAmount())
                .newSenderBalance(entity.getNewSenderBalance())
                .newReceiverBalance(entity.getNewReceiverBalance())
                .status(TransferTransaction.Status.valueOf(entity.getStatus()))
                .reason(entity.getReason())
                .build();
    }

    private TransactionEntity transactionEntity(AccountId senderId, AccountId receiverId, Long amount, String requestId) {
        var entity = new TransactionEntity();

        entity.setSenderId(senderId.getValue());
        entity.setReceiverId(receiverId.getValue());
        entity.setAmount(amount);
        entity.setStatus("SUCCESS");
        entity.setRequestId(UUID.fromString(requestId));

        return entity;
    }

    private TransactionEventEntity transactionEventEntity(TransactionEntity transactionEntity) {
        var entity = new TransactionEventEntity();

        var requestId = transactionEntity.getRequestId();
        var senderId = transactionEntity.getSenderId();
        var receiverId = transactionEntity.getReceiverId();
        var amount = transactionEntity.getAmount();
        var newSenderBalance = transactionEntity.getNewSenderBalance();
        var newReceiverBalance = transactionEntity.getNewReceiverBalance();
        var status = transactionEntity.getStatus();

        entity.setRequestId(requestId);
        entity.setSenderId(senderId);
        entity.setReceiverId(receiverId);
        entity.setAmount(amount);
        entity.setNewSenderBalance(newSenderBalance);
        entity.setNewReceiverBalance(newReceiverBalance);
        entity.setStatus(status);
        entity.setReason(transactionEntity.getReason());

        return entity;
    }

    private static class Context {
        Long senderAccountId;
        Long receiverAccountId;
        AccountEntity senderEntity;
        AccountEntity receiverEntity;
        Long amount;
        String requestId;
        TransactionEntity transactionEntity;

        public Long getSenderAccountId() {
            return senderAccountId;
        }

        public void setSenderAccountId(Long senderAccountId) {
            this.senderAccountId = senderAccountId;
        }

        public Long getReceiverAccountId() {
            return receiverAccountId;
        }

        public void setReceiverAccountId(Long receiverAccountId) {
            this.receiverAccountId = receiverAccountId;
        }

        public AccountEntity getSenderEntity() {
            return senderEntity;
        }

        public void setSenderEntity(AccountEntity senderEntity) {
            this.senderEntity = senderEntity;
        }

        public AccountEntity getReceiverEntity() {
            return receiverEntity;
        }

        public void setReceiverEntity(AccountEntity receiverEntity) {
            this.receiverEntity = receiverEntity;
        }

        public Long getAmount() {
            return amount;
        }

        public void setAmount(Long amount) {
            this.amount = amount;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public TransactionEntity getTransactionEntity() {
            return transactionEntity;
        }

        public void setTransactionEntity(TransactionEntity transactionEntity) {
            this.transactionEntity = transactionEntity;
        }
    }
}
