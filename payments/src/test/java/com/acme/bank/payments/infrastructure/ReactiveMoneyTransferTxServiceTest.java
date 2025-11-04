package com.acme.bank.payments.infrastructure;

import com.acme.bank.payments.EmbeddedR2dbcConfiguration;
import com.acme.bank.payments.domain.exception.AccountNotFoundException;
import com.acme.bank.payments.domain.exception.InsufficientFundsException;
import com.acme.bank.payments.domain.frauddetection.FraudDetectionService;
import com.acme.bank.payments.domain.model.FraudInspectionResult;
import com.acme.bank.payments.domain.model.TransferTransaction;
import com.acme.bank.payments.domain.vo.AccountId;
import com.acme.bank.payments.infrastructure.persistence.entity.relational.AccountEntity;
import com.acme.bank.payments.infrastructure.persistence.repository.DatabaseAccountRepository;
import com.acme.bank.payments.infrastructure.persistence.repository.DatabaseTransactionEventRepository;
import com.acme.bank.payments.infrastructure.persistence.repository.DatabaseTransactionRepository;
import com.acme.bank.payments.infrastructure.service.ReactiveMoneyTransferTxService;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Sql("classpath:/db/migrations/V1__initial.sql")
@AutoConfigureEmbeddedDatabase
@Import(EmbeddedR2dbcConfiguration.class)



public class ReactiveMoneyTransferTxServiceTest {
    private static Logger log = LoggerFactory.getLogger(ReactiveMoneyTransferTxServiceTest.class);

    @Autowired
    private DatabaseAccountRepository accountRepository;

    @Autowired
    private DatabaseTransactionRepository transactionRepository;

    @Autowired
    private DatabaseTransactionEventRepository transactionEventRepository;

    @Autowired
    private TransactionalOperator txOperator;

    @MockitoBean
    private FraudDetectionService fraudDetectionService;

    @MockitoBean
    private ReactiveStringRedisTemplate redisTemplate;

    private ReactiveMoneyTransferTxService service;

    private final Long ACCOUNTS_COUNT = 100L;
    private final Long INITIAL_BALANCE = 1000L;
    private final Long TRANSFER_AMOUNT = 1L;
    private final Long CLIENT_ID = 1L;

    private static final Random random = ThreadLocalRandom.current();

    @BeforeEach
    public void init() {
        var inspectionResult = new FraudInspectionResult(true, "");
        when(fraudDetectionService.inspect(any())).thenReturn(CompletableFuture.completedFuture(inspectionResult));

        service = new ReactiveMoneyTransferTxService(
                accountRepository,
                transactionRepository,
                transactionEventRepository,
                txOperator,
                fraudDetectionService,
                new ObjectMapper(),
                redisTemplate
        );
    }

    @Test
    public void account_not_found_test() {
        generateAccounts(1, INITIAL_BALANCE);

        var senderId = AccountId.of(0L);
        var receiverId = AccountId.of(1L);

        var future = service.executeTransaction(
            senderId,
            receiverId,
            TRANSFER_AMOUNT,
            UUID.randomUUID().toString()
        );

        assertThat(future).isNotNull();
        assertThatThrownBy(future::get).hasCauseExactlyInstanceOf(AccountNotFoundException.class);
    }

    @Test
    public void not_enough_funds_test() {
        generateAccounts(2, INITIAL_BALANCE);

        var senderId = AccountId.of(1L);
        var receiverId = AccountId.of(2L);

        var future = service.executeTransaction(
            senderId,
            receiverId,
            INITIAL_BALANCE + 100,
            UUID.randomUUID().toString()
        );

        assertThat(future).isNotNull();
        assertThatThrownBy(future::get).hasCauseExactlyInstanceOf(InsufficientFundsException.class);
    }

    @Test
    public void concurrent_updates_test() throws ExecutionException, InterruptedException {
        generateAccounts(ACCOUNTS_COUNT, INITIAL_BALANCE).blockLast();

        var txCount = 500;

        List<Mono<TransferTransaction>> monoList = IntStream.range(0, txCount)
                .mapToObj(this::executeTransaction)
                .toList();

        var flux = Flux.concat(monoList);
        var txList = flux.collectList().block();

        assertThat(txList).isNotNull();

        var successCount = txList.stream()
                .filter(TransferTransaction::isSuccess)
                .count();

        assertThat(successCount).isEqualTo(txCount);

        var totalBalance = accountRepository.findAll()
                .flatMap(entity -> Mono.just(entity.getBalance()))
                .collectList()
                .block()
                .stream()
                .reduce(0L, Long::sum);

        assertThat(totalBalance)
                .as("Total balance across all accounts should not changed regardless how many transactions were executed")
                .isEqualTo(ACCOUNTS_COUNT * INITIAL_BALANCE);
    }

    private Mono<TransferTransaction> executeTransaction(int i) {
        var senderId = 1 + random.nextLong(ACCOUNTS_COUNT - 1);
        var receiverId = 1 + random.nextLong(ACCOUNTS_COUNT - 1);

        if (senderId == receiverId) {
            receiverId++;
        }

        var requestId = UUID.randomUUID().toString();
        return Mono.fromFuture(
            service.executeTransaction(
                AccountId.of(senderId),
                AccountId.of(receiverId),
                TRANSFER_AMOUNT,
                requestId
            )
        );
    }

    private Flux<AccountEntity> generateAccounts(long count, long balance) {
        var entities = LongStream.range(0, count).mapToObj(i -> {
            var accountEntity = new AccountEntity();
            accountEntity.setClientId(CLIENT_ID);
            accountEntity.setBalance(balance);

            return accountRepository.save(accountEntity);
        }).toList();

        return accountRepository.deleteAll()
                .thenMany(Flux.concat(entities));
    }
}
