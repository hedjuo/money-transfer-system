package com.acme.bank.payments.infrastructure.idempotence;

import com.acme.bank.payments.domain.idempotence.MoneyTransferOperation;
import com.acme.bank.payments.domain.idempotence.MoneyTransferOperationRequest;
import com.acme.bank.payments.domain.model.TransferTransaction;
import com.acme.bank.payments.domain.vo.AccountId;
import com.acme.bank.payments.domain.vo.TransactionId;
import com.acme.bank.payments.infrastructure.TestcontainersConfiguration;
import io.github.resilience4j.core.EventConsumer;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.testcontainers.containers.GenericContainer;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RedisIdempotenceServiceTest {
    private final Random random = ThreadLocalRandom.current();

    @Test
    void two_exact_requests_test() {
        ReactiveStringRedisTemplate redisTemplate = mock(ReactiveStringRedisTemplate.class);

        var reactiveValueOperations = (ReactiveValueOperations<String, String>) mock(ReactiveValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(reactiveValueOperations);

        when(reactiveValueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(Mono.just(true));

        when(reactiveValueOperations.set(anyString(), anyString(), any(Duration.class)))
                .thenReturn(Mono.just(true));

        var requestId = UUID.randomUUID();
        var idempotencyKey = requestId.toString();
        var senderId = random.nextLong();
        var receiverId = random.nextLong();
        var request = new MoneyTransferOperationRequest(
            idempotencyKey,
            senderId,
            receiverId,
            1L
        );
        var successTransaction = TransferTransaction.builder()
                .id(TransactionId.of(1L))
                .requestId(requestId)
                .senderId(AccountId.of(1L))
                .receiverId(AccountId.of(2L))
                .amount(1L)
                .newSenderBalance(0L)
                .newReceiverBalance(3L)
                .status(TransferTransaction.Status.SUCCESS)
                .build();

        Function<MoneyTransferOperationRequest, CompletableFuture<TransferTransaction>> function = req -> completedFuture(successTransaction);

        RedisIdempotenceService service = new RedisIdempotenceService(redisTemplate);

        var operation = new MoneyTransferOperation(request, function);
        var future = service.execute(idempotencyKey, operation);
        var actualResult = future.join();

        when(reactiveValueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(Mono.just(false));

        var retryFuture = service.execute(idempotencyKey, operation);
        var retryResult = future.join();

        verify(redisTemplate, times(4)).opsForValue();
        verify(reactiveValueOperations, times(2)).setIfAbsent(anyString(), anyString(), any(Duration.class));
        verify(reactiveValueOperations).set(anyString(), anyString(), any(Duration.class));
        verify(reactiveValueOperations).get(anyString());
    }
}
