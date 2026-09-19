package com.ecommerce.finance.models;

public class RefundData {
    private String reason;
    private double amount;

    public RefundData(String reason, double amount) {
        this.reason = reason;
        this.amount = amount;
    }

    public String getReason() { return reason; }
    public double getAmount() { return amount; }
}