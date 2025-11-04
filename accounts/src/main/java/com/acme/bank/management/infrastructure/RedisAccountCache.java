package com.acme.bank.management.infrastructure;

import com.acme.bank.management.domain.AccountCache;
import com.acme.bank.management.dto.AccountDTO;
import com.acme.bank.payments.domain.model.Account;
import com.acme.bank.payments.domain.vo.AccountId;
import com.acme.bank.payments.domain.vo.ClientId;
import com.acme.bank.payments.domain.vo.Version;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class RedisAccountCache implements AccountCache {
    private final ReactiveRedisTemplate<String, AccountDTO> redisTemplate;

    public RedisAccountCache(ReactiveRedisTemplate<String, AccountDTO> redisTemplate ) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public CompletableFuture<List<Account>> findByClientId(ClientId clientId) {
        var clientKeyPattern = "client:" + clientId.getValue() + ":acc:*";

        return redisTemplate
            .keys(clientKeyPattern)
            .flatMap( accountKey ->
                redisTemplate
                    .opsForValue()
                    .get(accountKey)
            )
            .map(this::toAccount)
            .collectList().toFuture();
    }
    
    @Override
    public CompletableFuture<Optional<Account>> findById(AccountId id) {
        return redisTemplate
            .keys("*:acc:" + id.getValue())
            .singleOrEmpty()
            .singleOptional()
            .flatMap(keyOptional ->
                keyOptional.map(
                key -> redisTemplate
                    .opsForValue()
                    .get(key)
                    .map(this::toAccount)
                    .map(Optional::ofNullable)
                )
                .orElseGet(() ->
                    Mono.just(Optional.empty())
                )
            ).toFuture();
    }

    @Override
    public CompletableFuture<Account> put(Account account) {
        var clientId = account.getClientId().getValue();
        var accountId = account.getId().getValue();
        var key = "client:" + clientId + ":acc:" + accountId;

        return redisTemplate.opsForValue()
                .set(key, toDto(account))
                .thenReturn(account)
                .toFuture();
    }

    private Account toAccount(AccountDTO accountDTO) {
        return Account.builder()
                .id(AccountId.of(accountDTO.id()))
                .clientId(ClientId.of(accountDTO.clientId()))
                .balance(accountDTO.balance())
                .version(Version.of(accountDTO.version()))
                .build();
    }

    private AccountDTO toDto(Account account) {
        return new AccountDTO(
                account.getId().getValue(),
                account.getClientId().getValue(),
                account.getBalance(),
                account.getVersion().getValue()
        );
    }
}
