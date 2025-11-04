package com.acme.bank.payments.application.delivery.rest;

import com.acme.bank.payments.application.delivery.rest.dto.TransactionDTO;
import com.acme.bank.payments.application.delivery.rest.dto.TransferRequestDTO;
import com.acme.bank.payments.domain.idempotence.IdempotenceService;
import com.acme.bank.payments.domain.idempotence.MoneyTransferOperation;
import com.acme.bank.payments.domain.idempotence.MoneyTransferOperationRequest;
import com.acme.bank.payments.domain.model.TransferTransaction;
import com.acme.bank.payments.domain.service.MoneyTransferTxService;
import com.acme.bank.payments.domain.vo.AccountId;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/payments")
public class MoneyTransferController {
    private final MoneyTransferTxService moneyTransferService;
    private final IdempotenceService idempotenceService;

    public MoneyTransferController(
        MoneyTransferTxService moneyTransferService,
        IdempotenceService idempotenceService
    ) {
        this.moneyTransferService = moneyTransferService;
        this.idempotenceService = idempotenceService;
    }

    @PostMapping("/transfer")
    public CompletableFuture<TransactionDTO> transferRandom(
        @RequestBody TransferRequestDTO request
    ) {
        var moneyTransferRequest = new MoneyTransferOperationRequest(
            request.requestId(),
            request.senderId(),
            request.receiverId(),
            request.amount()
        );

        var operation = new MoneyTransferOperation(moneyTransferRequest, this::processTransaction);

        return idempotenceService.execute(request.requestId(), operation)
                .thenApply(this::toTransactionDto);
    }

    private CompletableFuture<TransferTransaction> processTransaction(MoneyTransferOperationRequest request) {
        return moneyTransferService.executeTransaction(
                AccountId.of(request.senderId()),
                AccountId.of(request.receiverId()),
                request.amount(),
                request.requestId()
        );
    }

    private TransactionDTO toTransactionDto(TransferTransaction tx) {
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
}
