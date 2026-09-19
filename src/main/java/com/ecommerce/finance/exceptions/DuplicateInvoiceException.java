package com.ecommerce.finance.exceptions;

public class DuplicateInvoiceException extends Exception {
    private int orderId;

    public DuplicateInvoiceException(String message, int orderId) {
        super(message);
        this.orderId = orderId;
    }

    public int getOrderId() { return orderId; }
}