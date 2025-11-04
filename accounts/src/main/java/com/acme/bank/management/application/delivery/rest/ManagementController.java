package com.acme.bank.management.application.delivery.rest;

import com.acme.bank.management.domain.ManagementService;
import com.acme.bank.management.dto.AccountDTO;
import com.acme.bank.management.dto.ClientDTO;
import com.acme.bank.management.dto.CreateAccountRequest;
import com.acme.bank.management.dto.CreateClientRequest;
import com.acme.bank.payments.domain.model.Account;
import com.acme.bank.payments.domain.model.Client;
import com.acme.bank.payments.domain.vo.AccountId;
import com.acme.bank.payments.domain.vo.ClientId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1")
public class ManagementController {
    private final ManagementService managementService;

    public ManagementController(ManagementService managementService) {
        this.managementService = managementService;
    }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public CompletableFuture<AccountDTO> createAccount(@RequestBody CreateAccountRequest request) {
        return managementService.createAccount(
            ClientId.of(request.clientId()),
            request.initialBalance()
        ).thenApply(this::accountDTO);
    }

    @GetMapping("/accounts/{id}")
    public CompletableFuture<AccountDTO> getAccount(@PathVariable Long id) {
        var accountId = AccountId.of(id);

        return managementService.findAccountById(accountId)
                .thenApply(this::accountDTO);
    }

    @PostMapping("/clients")
    @ResponseStatus(HttpStatus.CREATED)
    public CompletableFuture<ClientDTO> createClient(@RequestBody CreateClientRequest request) {
        return managementService.createClient(request.name())
                .thenApply(this::clientDTO);
    }

    @GetMapping("/clients/{id}")
    public CompletableFuture<ClientDTO> createClient(@PathVariable Long id) {
        return managementService.findClientById(id)
                .thenApply(this::clientDTO);
    }

    @GetMapping("/clients/{id}/accounts")
    public CompletableFuture<List<AccountDTO>> getAccountByClientId(@PathVariable Long id) {
        var clientId = ClientId.of(id);

        return managementService.findAccountsByClientId(clientId)
                .thenApply(this::accountDTOList);
    }

    private ClientDTO clientDTO(Optional<Client> optional) {
        return optional
                .map(this::clientDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private ClientDTO clientDTO(Client client) {
        return new ClientDTO(
                client.getId().getValue(),
                client.getName()
        );
    }

    private AccountDTO accountDTO(Optional<Account> optional) {
        return optional.map(this::accountDTO)
                       .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private List<AccountDTO> accountDTOList(List<Account> accounts) {
        return accounts.stream()
                .map(this::accountDTO)
                .toList();
    }

    private AccountDTO accountDTO(Account account) {
        return new AccountDTO(
            account.getId().getValue(),
            account.getClientId().getValue(),
            account.getBalance(),
            account.getVersion().getValue());
    }
}
