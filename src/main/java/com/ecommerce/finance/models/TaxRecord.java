package com.ecommerce.finance.models;

import java.time.LocalDate;

public class TaxRecord {
    private int taxId;
    private int orderId;
    private double grossAmount;
    private double taxRate;
    private double taxAmount;
    private LocalDate recordDate;

    public TaxRecord(int taxId, int orderId, double grossAmount, double taxRate, double taxAmount, LocalDate recordDate) {
        this.taxId = taxId;
        this.orderId = orderId;
        this.grossAmount = grossAmount;
        this.taxRate = taxRate;
        this.taxAmount = taxAmount;
        this.recordDate = recordDate;
    }

    public int getTaxId() { return taxId; }
    public int getOrderId() { return orderId; }
    public double getGrossAmount() { return grossAmount; }
    public double getTaxRate() { return taxRate; }
    public double getTaxAmount() { return taxAmount; }
    public LocalDate getRecordDate() { return recordDate; }
}