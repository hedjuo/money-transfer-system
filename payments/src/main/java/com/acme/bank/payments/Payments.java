package com.acme.bank.payments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
public class Payments {
    static void main(String[] args) {
        SpringApplication.run(Payments.class);
    }
}
