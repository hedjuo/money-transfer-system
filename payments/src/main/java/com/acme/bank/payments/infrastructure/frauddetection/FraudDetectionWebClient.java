package com.acme.bank.payments.infrastructure.frauddetection;

import com.acme.bank.payments.domain.frauddetection.FraudDetectionClient;
import com.acme.bank.payments.domain.model.FraudInspectionRequest;
import com.acme.bank.payments.domain.model.FraudInspectionResult;
import com.acme.bank.payments.infrastructure.frauddetection.dto.FraudInspectionResultDto;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.concurrent.CompletableFuture;

import static java.time.Duration.ofMillis;
import static java.time.Duration.ofSeconds;

import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;

@Component
public class FraudDetectionWebClient implements FraudDetectionClient {
    private static final Logger log = LoggerFactory.getLogger(FraudDetectionWebClient.class);
    private final WebClient fraudDetectionClient;
    private final CircuitBreaker circuitBreaker;

    public FraudDetectionWebClient(
        @Qualifier("fraudDetectionClient") WebClient client,
        @Qualifier("fraudDetectionCB") CircuitBreaker circuitBreaker
    ) {
        this.fraudDetectionClient =  client;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public CompletableFuture<FraudInspectionResult> inspect(FraudInspectionRequest request) {
        return fraudDetectionClient
                .post()
                .uri("/api/v1/transaction/inspect")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), FraudInspectionRequest.class)
                .retrieve()
                .bodyToMono(FraudInspectionResultDto.class)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .retryWhen(this.retrySpec())
                .doOnError(throwable -> log.error(throwable.getMessage(), throwable))
                .onErrorResume(this::fallback)
                .map(this::toFraudInspectionResult)
                .toFuture();
    }

    private Mono<FraudInspectionResultDto> fallback(Throwable t) {
        return Mono.just(new FraudInspectionResultDto(false, "Unable to verify request. Fraud detection service is unavailable"));
    }

    private FraudInspectionResult toFraudInspectionResult(FraudInspectionResultDto dto) {
        return new FraudInspectionResult(dto.getSuccess(), dto.getMessage());
    }

    private Retry retrySpec() {
        return Retry
                .backoff(10, ofMillis(100))
                .multiplier(3)
                .maxBackoff(ofSeconds(3))
                .doBeforeRetry(this::logRetry);
    }

    private void logRetry(Retry.RetrySignal  signal) {
        var ex = signal.failure();
        log.error(ex.getMessage(), ex);
        log.info("Retrying inspection request. Attempt {}", signal.totalRetries() + 1);
    }
}
