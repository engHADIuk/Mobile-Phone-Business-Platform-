package com.ecommerce.finance.models;

import java.time.LocalDate;

public class ProfitLossStatement {
    private LocalDate startDate;
    private LocalDate endDate;
    private double totalRevenue;
    private double totalCOGS;
    private double totalExpenses;
    private double grossProfit;
    private double netProfit;
    private String profitMargin;

    public ProfitLossStatement(LocalDate startDate, LocalDate endDate, double totalRevenue,
                               double totalCOGS, double totalExpenses, double grossProfit,
                               double netProfit, String profitMargin) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalRevenue = totalRevenue;
        this.totalCOGS = totalCOGS;
        this.totalExpenses = totalExpenses;
        this.grossProfit = grossProfit;
        this.netProfit = netProfit;
        this.profitMargin = profitMargin;
    }

    // Getters
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public double getTotalRevenue() { return totalRevenue; }
    public double getTotalCOGS() { return totalCOGS; }
    public double getTotalExpenses() { return totalExpenses; }
    public double getGrossProfit() { return grossProfit; }
    public double getNetProfit() { return netProfit; }
    public String getProfitMargin() { return profitMargin; }

    public boolean isProfit() { return netProfit > 0; }
    public boolean hasRevenue() { return totalRevenue > 0; }
}