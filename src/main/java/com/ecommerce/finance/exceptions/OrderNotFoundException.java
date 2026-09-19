package com.ecommerce.finance.exceptions;

public class OrderNotFoundException extends Exception {
    private int orderId;

    public OrderNotFoundException(String message, int orderId) {
        super(message);
        this.orderId = orderId;
    }

    public int getOrderId() { return orderId; }
}