package com.acme.bank.payments.infrastructure.idempotence;

import com.acme.bank.payments.application.delivery.rest.dto.TransactionDTO;
import com.acme.bank.payments.domain.idempotence.IdempotenceService;
import com.acme.bank.payments.domain.idempotence.IdempotentOperation;
import com.acme.bank.payments.domain.model.TransferTransaction;
import com.acme.bank.payments.domain.vo.AccountId;
import com.acme.bank.payments.domain.vo.TransactionId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static java.time.Duration.ofSeconds;

@Component
public class RedisIdempotenceService implements IdempotenceService {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(RedisIdempotenceService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final ReactiveStringRedisTemplate redisTemplate;

    public RedisIdempotenceService(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public CompletableFuture<TransferTransaction> execute(String idempotencyKey, IdempotentOperation idempotentOperation) {
        return setIdempotencyKey(idempotencyKey)
                .flatMap(newKey -> {
                    if (newKey) {
                        return Mono.fromFuture(idempotentOperation.execute())
                                .flatMap(this::cacheResult);
                    }
                    return getResult(idempotencyKey, ofSeconds(2));
                })
                .toFuture();
    }

    private Mono<TransferTransaction> cacheResult(TransferTransaction transaction) {
        var redisKey = "idemp:" + transaction.getRequestId() + ":resp";
        var dto = toTransactionDto(transaction);
        try {
            var value = objectMapper.writeValueAsString(dto);
            return redisTemplate.opsForValue()
                    .set(redisKey, value, Duration.ofMinutes(3))
                    .thenReturn(transaction);
        } catch (JsonProcessingException e) {
            log.error("Error serializing transaction result", e);
            return Mono.error(new RuntimeException("Error serializing transaction result", e));
        }
    }

    private Mono<TransferTransaction> getResult(String idempotencyKey, Duration timeout) {
        return pollForResult(idempotencyKey, timeout)
                .flatMap(this::toTransferTransaction)
                .onErrorResume(TimeoutException.class, e -> Mono.empty());
    }

    private Mono<String> pollForResult(String idempotencyKey, Duration timeout) {
        var redisKey = "idemp:" + idempotencyKey + ":resp";
        return redisTemplate.opsForValue()
                .get(redisKey)
                .repeatWhenEmpty(flux -> flux.delayElements(Duration.ofMillis(30))
                        .timeout(timeout));
    }

    private static TransactionDTO toTransactionDto(TransferTransaction tx) {
        return new TransactionDTO(
                tx.getId().getValue(),
                tx.getRequestId().toString(),
                tx.getSenderId().getValue(),
                tx.getReceiverId().getValue(),
                tx.getAmount(),
                tx.getNewSenderBalance(),
                tx.getNewReceiverBalance(),
                tx.isSuccess() ? "SUCCESS" : "FAILED"
        );
    }

    private Mono<TransferTransaction> toTransferTransaction(String json) {
        try {
            var dto = objectMapper.readValue(json, TransactionDTO.class);
            return Mono.just(toTransferTransaction(dto));
        } catch (Exception e) {
            log.error("Error deserializing cached response", e);
            return Mono.error(new RuntimeException("Error deserializing cached response", e));
        }
    }

    private static TransferTransaction toTransferTransaction(TransactionDTO dto) {
        return TransferTransaction.builder()
                .id(TransactionId.of(dto.id()))
                .requestId(UUID.fromString(dto.requestId()))
                .senderId(AccountId.of(dto.senderId()))
                .receiverId(AccountId.of(dto.receiverId()))
                .amount(dto.amount())
                .newSenderBalance(dto.newSenderBalance())
                .newReceiverBalance(dto.newReceiverBalance())
                .status(TransferTransaction.Status.valueOf(dto.status()))
                .build();
    }

    private Mono<TransactionDTO> deserializeResponse(String json) {
        try {
            var dto = objectMapper.readValue(json, TransactionDTO.class);
            return Mono.just(dto);
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Error deserializing cached response", e));
        }
    }

    private Mono<Boolean> setIdempotencyKey(String idempotencyKey) {
        var redisKey = "idemp:" + idempotencyKey + ":req";

        return redisTemplate
                .opsForValue()
                .setIfAbsent(redisKey, "1", Duration.ofMinutes(1));
    }
}
