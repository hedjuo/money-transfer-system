package com.acme.bank.payments.infrastructure.frauddetection.dto;

public class FraudInspectionResultDto {
    public Boolean success;

    public String message;

    public FraudInspectionResultDto() {}

    public FraudInspectionResultDto(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
