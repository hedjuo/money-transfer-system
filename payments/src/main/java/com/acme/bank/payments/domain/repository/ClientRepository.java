package com.acme.bank.payments.domain.repository;

import com.acme.bank.payments.domain.model.Client;
import com.acme.bank.payments.domain.vo.ClientId;

import java.util.concurrent.CompletableFuture;

public interface ClientRepository {
    CompletableFuture<Client> findById(ClientId id);
}
