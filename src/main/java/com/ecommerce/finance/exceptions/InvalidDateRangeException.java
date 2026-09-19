package com.ecommerce.finance.exceptions;

public class InvalidDateRangeException extends Exception {
    private String startDate;
    private String endDate;

    public InvalidDateRangeException(String message, String startDate, String endDate) {
        super(message);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
}