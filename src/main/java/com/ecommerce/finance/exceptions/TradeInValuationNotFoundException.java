package com.ecommerce.finance.exceptions;

public class TradeInValuationNotFoundException extends Exception {
    private String deviceModel;
    private String conditionGrade;

    public TradeInValuationNotFoundException(String message, String deviceModel, String conditionGrade) {
        super(message);
        this.deviceModel = deviceModel;
        this.conditionGrade = conditionGrade;
    }

    public String getDeviceModel() { return deviceModel; }
    public String getConditionGrade() { return conditionGrade; }
}