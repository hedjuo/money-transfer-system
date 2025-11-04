package com.acme.bank.management.infrastructure.persistence;

import com.acme.bank.management.domain.ClientRepository;
import com.acme.bank.payments.domain.model.Client;
import com.acme.bank.payments.domain.vo.ClientId;
import com.acme.bank.payments.infrastructure.persistence.entity.relational.ClientEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class DatabaseClientRepository implements ClientRepository {
    private ReactiveClientRepository delegate;

    public DatabaseClientRepository(
        ReactiveClientRepository delegate
    ) {
        this.delegate = delegate;
    }


    @Override
    public CompletableFuture<Client> save(Client client) {
        var entity = new ClientEntity();
        entity.setName(client.getName());

        return delegate
                   .save(entity)
                   .map(this::toClient)
                   .toFuture();
    }

    @Override
    public CompletableFuture<Optional<Client>> findById(ClientId id) {
        return delegate.findById(id.getValue())
                        .singleOptional()
                        .map(optional -> optional.map(this::toClient))
                        .toFuture();
    }

    private Client toClient(ClientEntity entity) {
        return Client.builder()
                .id(ClientId.of(entity.getId()))
                .name(entity.getName())
                .build();
    }
}
