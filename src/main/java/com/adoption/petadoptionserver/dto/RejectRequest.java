package com.adoption.petadoptionserver.dto;

public class RejectRequest {
    private String reason;

    public RejectRequest() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}