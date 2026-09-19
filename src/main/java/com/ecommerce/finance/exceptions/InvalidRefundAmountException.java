package com.ecommerce.finance.exceptions;

public class InvalidRefundAmountException extends Exception {
    private double requestedAmount;
    private double maxAmount;

    public InvalidRefundAmountException(String message, double requestedAmount, double maxAmount) {
        super(message);
        this.requestedAmount = requestedAmount;
        this.maxAmount = maxAmount;
    }

    public double getRequestedAmount() { return requestedAmount; }
    public double getMaxAmount() { return maxAmount; }
}