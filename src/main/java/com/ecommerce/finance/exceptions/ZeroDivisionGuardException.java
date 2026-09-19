package com.ecommerce.finance.exceptions;

public class ZeroDivisionGuardException extends Exception {
    private String operation;

    public ZeroDivisionGuardException(String message, String operation) {
        super(message);
        this.operation = operation;
    }

    public String getOperation() { return operation; }
}