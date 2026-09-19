package com.ecommerce.finance.models;

import java.time.LocalDate;

public class BudgetEntry {
    private int entryId;
    private String category;
    private double plannedAmount;
    private String period;
    private LocalDate createdDate;

    public int getEntryId() { return entryId; }
    public void setEntryId(int entryId) { this.entryId = entryId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPlannedAmount() { return plannedAmount; }
    public void setPlannedAmount(double plannedAmount) { this.plannedAmount = plannedAmount; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public boolean isValid() {
        return category != null && !category.isEmpty() && plannedAmount >= 0;
    }
}