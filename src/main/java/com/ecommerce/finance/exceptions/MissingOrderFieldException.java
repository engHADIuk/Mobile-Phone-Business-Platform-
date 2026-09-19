package com.ecommerce.finance.exceptions;

public class MissingOrderFieldException extends Exception {
    private String fieldName;

    public MissingOrderFieldException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() { return fieldName; }
}