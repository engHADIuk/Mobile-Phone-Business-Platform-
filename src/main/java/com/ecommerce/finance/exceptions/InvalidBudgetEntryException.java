package com.ecommerce.finance.exceptions;

public class InvalidBudgetEntryException extends Exception {
    private String category;
    private double plannedAmount;

    public InvalidBudgetEntryException(String message, String category, double plannedAmount) {
        super(message);
        this.category = category;
        this.plannedAmount = plannedAmount;
    }

    public String getCategory() { return category; }
    public double getPlannedAmount() { return plannedAmount; }
}