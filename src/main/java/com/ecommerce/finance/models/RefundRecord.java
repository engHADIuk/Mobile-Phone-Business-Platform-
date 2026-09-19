package com.ecommerce.finance.models;

import java.time.LocalDate;

public class RefundRecord {
    private int refundId;
    private int orderId;
    private int customerId;
    private double amount;
    private String reason;
    private LocalDate refundDate;
    private String status;

    // Getters
    public int getRefundId() { return refundId; }
    public void setRefundId(int refundId) { this.refundId = refundId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDate getRefundDate() { return refundDate; }
    public void setRefundDate(LocalDate refundDate) { this.refundDate = refundDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}