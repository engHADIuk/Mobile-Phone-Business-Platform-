package com.ecommerce.finance.models;

import java.time.LocalDate;
import java.util.List;

public class TaxReport {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<TaxRecord> records;
    private double grossSales;
    private double totalVAT;
    private double netRevenue;

    public TaxReport(LocalDate startDate, LocalDate endDate, List<TaxRecord> records,
                     double grossSales, double totalVAT, double netRevenue) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.records = records;
        this.grossSales = grossSales;
        this.totalVAT = totalVAT;
        this.netRevenue = netRevenue;
    }

    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public List<TaxRecord> getRecords() { return records; }
    public double getGrossSales() { return grossSales; }
    public double getTotalVAT() { return totalVAT; }
    public double getNetRevenue() { return netRevenue; }
}