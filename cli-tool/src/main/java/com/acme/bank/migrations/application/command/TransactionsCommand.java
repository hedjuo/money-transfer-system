package com.acme.bank.migrations.application.command;

import com.acme.bank.migrations.infrastructure.persistence.repository.TransactionEventRepository;
import com.acme.bank.migrations.infrastructure.persistence.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent("Transaction management")
public class TransactionsCommand {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionEventRepository transactionEventRepository;


    @ShellMethod(key = "tx-count", value = "Total transactions count")
    public String txTotal() {
        return "Total transactions: " + transactionRepository.count();
    }

    @ShellMethod(key = "tx-event-count", value = "Total transaction events count")
    public String txEventTotal() {
        return "Total transactions: " + transactionEventRepository.count();
    }

    @ShellMethod(key = "tx-reset", value = "Delete all transactions and events")
    public String resetBalance() {
        transactionRepository.deleteAll();
        transactionEventRepository.deleteAll();
        return "OK";
    }
}
