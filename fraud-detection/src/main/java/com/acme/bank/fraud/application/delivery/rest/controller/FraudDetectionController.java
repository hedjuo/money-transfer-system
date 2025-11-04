package com.acme.bank.fraud.application.delivery.rest.controller;

import com.acme.bank.fraud.application.delivery.rest.dto.InspectionRequest;
import com.acme.bank.fraud.application.delivery.rest.dto.InspectionResponseDto;
import com.acme.bank.fraud.domain.inspection.FraudDetector;

import com.acme.bank.fraud.domain.vo.AccountId;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/transaction")
public class FraudDetectionController {

    private final FraudDetector fraudDetector;

    public FraudDetectionController(FraudDetector fraudDetector) {
        this.fraudDetector = fraudDetector;
    }

    @PostMapping("/inspect")
    public CompletableFuture<InspectionResponseDto> inspect(@RequestBody InspectionRequest payload) {
        var senderId = payload.senderId();
        var receiverId = payload.receiverId();
        var amount = payload.amount();

        var request = buildRequest(senderId, receiverId, amount);
        return fraudDetector.inspect(request)
                .thenApply(model -> new InspectionResponseDto(model.isSuccess(), model.getReason()));
    }

    private com.acme.bank.fraud.domain.model.InspectionRequest buildRequest(
        Long senderId,
        Long receiverId,
        Long amount
    ) {
        var modelBuilder = com.acme.bank.fraud.domain.model.InspectionRequest.builder();

        return modelBuilder
                .senderId(AccountId.of(senderId))
                .receiverId(AccountId.of(receiverId))
                .amount(amount)
                .build();
    }
}
