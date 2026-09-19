package com.ecommerce.finance.models;

public class SalesData {
    private String date;
    private double amount;
    private int orders;

    public SalesData(String date, double amount, int orders) {
        this.date = date;
        this.amount = amount;
        this.orders = orders;
    }

    public String getDate() { return date; }
    public double getAmount() { return amount; }
    public int getOrders() { return orders; }
}