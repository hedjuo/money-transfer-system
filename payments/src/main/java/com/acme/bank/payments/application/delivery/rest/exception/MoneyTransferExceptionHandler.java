package com.acme.bank.payments.application.delivery.rest.exception;

import com.acme.bank.payments.domain.exception.AccountNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class MoneyTransferExceptionHandler {
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Mono<String>> handleAccountNotFoundException(AccountNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Mono.just(ex.getMessage()));
    }
}
