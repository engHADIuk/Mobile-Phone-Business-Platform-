package com.ecommerce.finance.models;

import java.time.LocalDate;
import java.util.List;

public class InstalmentPlan {
    private int planId;
    private int customerId;
    private int orderId;
    private double totalValue;
    private int instalmentsCount;
    private double instalmentAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private List<PaymentReceipt> receipts;

    public int getPlanId() { return planId; }
    public void setPlanId(int planId) { this.planId = planId; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public double getTotalValue() { return totalValue; }
    public void setTotalValue(double totalValue) { this.totalValue = totalValue; }
    public int getInstalmentsCount() { return instalmentsCount; }
    public void setInstalmentsCount(int instalmentsCount) { this.instalmentsCount = instalmentsCount; }
    public double getInstalmentAmount() { return instalmentAmount; }
    public void setInstalmentAmount(double instalmentAmount) { this.instalmentAmount = instalmentAmount; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<PaymentReceipt> getReceipts() { return receipts; }
    public void setReceipts(List<PaymentReceipt> receipts) { this.receipts = receipts; }
}