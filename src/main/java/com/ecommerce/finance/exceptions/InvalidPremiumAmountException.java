package com.ecommerce.finance.exceptions;

public class InvalidPremiumAmountException extends Exception {
    private int addonId;
    private double amount;

    public InvalidPremiumAmountException(String message, int addonId, double amount) {
        super(message);
        this.addonId = addonId;
        this.amount = amount;
    }

    public int getAddonId() { return addonId; }
    public double getAmount() { return amount; }
}