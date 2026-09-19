package com.ecommerce.finance.models;

public class RevenueRow {
    private String category;
    private double revenue;
    private int orderCount;

    public RevenueRow(String category, double revenue, int orderCount) {
        this.category = category;
        this.revenue = revenue;
        this.orderCount = orderCount;
    }

    public String getCategory() { return category; }
    public double getRevenue() { return revenue; }
    public int getOrderCount() { return orderCount; }
}