package com.ecommerce.finance.models;

import java.util.List;

public class WarrantyReport {
    private List<WarrantyRow> rows;
    private double totalRevenue;

    public WarrantyReport(List<WarrantyRow> rows, double totalRevenue) {
        this.rows = rows;
        this.totalRevenue = totalRevenue;
    }

    public List<WarrantyRow> getRows() { return rows; }
    public double getTotalRevenue() { return totalRevenue; }
    public boolean isEmpty() { return rows == null || rows.isEmpty(); }
}

