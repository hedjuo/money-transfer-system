package com.acme.bank.management.domain;

import com.acme.bank.payments.domain.model.Client;
import com.acme.bank.payments.domain.vo.ClientId;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ClientRepository {
    CompletableFuture<Client> save(Client client);
    CompletableFuture<Optional<Client>> findById(ClientId id);
}
