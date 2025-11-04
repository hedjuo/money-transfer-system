package com.acme.bank.migrations.application.command;

import com.acme.bank.migrations.infrastructure.persistence.repository.AccountRepository;
import com.acme.bank.payments.infrastructure.persistence.entity.jpa.AccountEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.stream.IntStream;

@ShellComponent("Account management")
public class AccountsCommand {

    @Autowired
    private AccountRepository accountRepository;

    @ShellMethod(key = "acc-generate", value = "Generate accounts")
    public String generateAccounts(@ShellOption Long clientId, @ShellOption Integer count, @ShellOption Long initialBalance) {
        var listMono = IntStream.range(0, count).mapToObj(i -> {
            var account = new AccountEntity();
            account.setClientId(clientId);
            account.setBalance(initialBalance);
            return account;
        })
        .map(accountRepository::save)
        .toList();

        return "OK. Generated " + listMono.size();
    }

    @ShellMethod(key = "acc-total-balance", value = "Total balance across all accounts")
    public String totalFunds() {
        return "Total funds across all accounts: " + accountRepository.totalAmountOfFunds();
    }

    @ShellMethod(key = "acc-reset-balance", value = "Set given balance to all accounts")
    public String resetBalance(@ShellOption Integer balance) {
        accountRepository.resetBalances(balance);
        return "OK";
    }

    @ShellMethod(key = "acc-delete-all", value = "Delete all accounts.")
    public String deleteAll() {
        accountRepository.truncate();
        return "OK";
    }

    @ShellMethod(key = "acc-count", value = "Returns how many accounts exists.")
    public String count() {
        return "Accounts count: " + accountRepository.count();
    }
}
