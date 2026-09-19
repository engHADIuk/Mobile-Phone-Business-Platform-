package com.ecommerce.finance.exceptions;

public class InvalidTaxRateException extends Exception {
    private double taxRate;

    public InvalidTaxRateException(String message, double taxRate) {
        super(message);
        this.taxRate = taxRate;
    }

    public double getTaxRate() { return taxRate; }
}