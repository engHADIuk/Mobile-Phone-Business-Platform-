package com.ecommerce.finance.models;

import java.time.LocalDate;

public class TradeInRecord {
    private int tradeId;
    private int orderId;
    private int customerId;
    private String deviceModel;
    private String conditionGrade;
    private double assessedValue;
    private LocalDate tradeDate;
    private String status;

    // Getters and Setters
    public int getTradeId() { return tradeId; }
    public void setTradeId(int tradeId) { this.tradeId = tradeId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getDeviceModel() { return deviceModel; }
    public void setDeviceModel(String deviceModel) { this.deviceModel = deviceModel; }
    public String getConditionGrade() { return conditionGrade; }
    public void setConditionGrade(String conditionGrade) { this.conditionGrade = conditionGrade; }
    public double getAssessedValue() { return assessedValue; }
    public void setAssessedValue(double assessedValue) { this.assessedValue = assessedValue; }
    public LocalDate getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDate tradeDate) { this.tradeDate = tradeDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}