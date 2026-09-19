package com.ecommerce.finance.models;

import java.time.LocalDate;

public class PaymentReceipt {
    private int receiptId;
    private int planId;
    private double amount;
    private LocalDate paymentDate;
    private String billingMonth;
    private String status;

    public int getReceiptId() { return receiptId; }
    public void setReceiptId(int receiptId) { this.receiptId = receiptId; }
    public int getPlanId() { return planId; }
    public void setPlanId(int planId) { this.planId = planId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public String getBillingMonth() { return billingMonth; }
    public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}