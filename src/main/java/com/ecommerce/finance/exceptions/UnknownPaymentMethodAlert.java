package com.ecommerce.finance.exceptions;

public class UnknownPaymentMethodAlert extends Exception {
    private String paymentMethod;

    public UnknownPaymentMethodAlert(String message, String paymentMethod) {
        super(message);
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentMethod() { return paymentMethod; }
}