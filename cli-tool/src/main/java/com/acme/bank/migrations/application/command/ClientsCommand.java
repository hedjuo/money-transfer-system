package com.acme.bank.migrations.application.command;

import com.acme.bank.migrations.infrastructure.persistence.repository.ClientRepository;
import com.acme.bank.payments.infrastructure.persistence.entity.jpa.ClientEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.List;

@ShellComponent("Client management")
public class ClientsCommand {
    @Autowired
    private ClientRepository clientRepository;

    @ShellMethod(key = "client-list", value = "List clients")
    public String listClients() {
        List<ClientEntity> clients = clientRepository.findAll();
        clients.forEach(client -> System.out.println(client.getId() + " - " + client.getName()));
        return "OK";
    }

    @ShellMethod(key = "client-create", value = "Create client")
    public String createClient(@ShellOption String name) {
        var clientEntity = new ClientEntity();
        clientEntity.setName(name);

        try {
            clientRepository.save(clientEntity);
            return "OK";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
