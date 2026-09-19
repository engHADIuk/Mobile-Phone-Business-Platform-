package com.ecommerce.finance.models;

import java.time.LocalDateTime;

public class FinancialAdjustment {
    private int adjId;
    private int orderId;
    private String adjType;
    private double value;
    private LocalDateTime timestamp;

    public int getAdjId() { return adjId; }
    public void setAdjId(int adjId) { this.adjId = adjId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public String getAdjType() { return adjType; }
    public void setAdjType(String adjType) { this.adjType = adjType; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}