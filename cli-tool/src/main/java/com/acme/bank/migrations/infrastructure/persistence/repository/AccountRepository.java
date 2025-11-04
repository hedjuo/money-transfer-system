package com.acme.bank.migrations.infrastructure.persistence.repository;

import com.acme.bank.payments.infrastructure.persistence.entity.jpa.AccountEntity;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import org.springframework.transaction.annotation.Transactional;

public interface  AccountRepository extends CrudRepository<AccountEntity, Long> {
    @Transactional
    @Modifying
    @NativeQuery("update account set balance = :balance where id > 0")
    Integer resetBalances(@Param("balance") long balance);

    @NativeQuery("select sum(balance) from account")
    Long totalAmountOfFunds();

    @NativeQuery("truncate table account restart identity ")
    void truncate();
}
