package com.acme.bank.migrations.infrastructure.persistence.repository;


import com.acme.bank.payments.infrastructure.persistence.entity.jpa.ClientEntity;
import org.springframework.data.repository.ListCrudRepository;

public interface ClientRepository extends ListCrudRepository<ClientEntity, Long> {}
