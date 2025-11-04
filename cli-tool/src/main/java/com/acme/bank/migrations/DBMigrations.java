package com.acme.bank.migrations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DBMigrations {
    public static void main(String[] args) {
        SpringApplication.run(DBMigrations.class, args);
    }
}
