package com.acme.bank.fraud.domain.model;

public class InspectionResponse {
    private  Boolean success;
    private String reason;

    private InspectionResponse(Boolean success, String reason) {
        this.success = success;
        this.reason = reason;
    }

    public static InspectionResponse success() {
        return new InspectionResponse(true, "Success");
    }

    public static InspectionResponse failed(String reason) {
        return new InspectionResponse(true, reason);
    }

    public Boolean isSuccess() {
        return success == true;
    }

    public String getReason() {
        return reason;
    }
}
