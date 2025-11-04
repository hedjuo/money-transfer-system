package com.acme.bank.payments.domain.model;

public class FraudInspectionResult {
    private Boolean success;
    private String message;

    public FraudInspectionResult(Boolean success,  String message) {
        this.success = success;
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String s) {
        this.message = s;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("FraudInspectionResult{");
        sb.append("success=").append(success);
        sb.append(", message='").append(message).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
