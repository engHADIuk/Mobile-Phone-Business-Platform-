package com.ecommerce.finance.models;

import java.util.List;

public class RevenueReport {
    private int reportId;
    private List<RevenueRow> rows;
    private double grandTotal;
    private String period;
    private String category;

    public RevenueReport(List<RevenueRow> rows, double grandTotal, String period, String category) {
        this.rows = rows;
        this.grandTotal = grandTotal;
        this.period = period;
        this.category = category;
    }

    public List<RevenueRow> getRows() { return rows; }
    public double getGrandTotal() { return grandTotal; }
    public String getPeriod() { return period; }
    public String getCategory() { return category; }
    public boolean isEmpty() { return rows == null || rows.isEmpty(); }
}

