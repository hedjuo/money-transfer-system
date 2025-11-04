package com.acme.bank.application.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @PostMapping("/payments")
    public ResponseEntity<Map<String, String>> paymentsFallback() {
        var payload = Map.of(
            "message", "Payments temporarily unavailable",
            "path", "/payments/transfer"
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(payload);
    }
}
