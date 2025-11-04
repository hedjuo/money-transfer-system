package com.acme.bank.management.infrastructure.persistence;

import com.acme.bank.payments.infrastructure.persistence.entity.relational.ClientEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Component;

@Component
public interface ReactiveClientRepository extends R2dbcRepository<ClientEntity, Long> {
}
