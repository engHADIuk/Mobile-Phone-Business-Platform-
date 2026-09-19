package com.ecommerce.finance.models;

public class BudgetVarianceRow {
    private String category;
    private double planned;
    private double actual;
    private double variance;
    private String percentVariance;
    private String colourCode;

    public BudgetVarianceRow(String category, double planned, double actual, 
                             double variance, String percentVariance, String colourCode) {
        this.category = category;
        this.planned = planned;
        this.actual = actual;
        this.variance = variance;
        this.percentVariance = percentVariance;
        this.colourCode = colourCode;
    }

    public String getCategory() { return category; }
    public double getPlanned() { return planned; }
    public double getActual() { return actual; }
    public double getVariance() { return variance; }
    public String getPercentVariance() { return percentVariance; }
    public String getColourCode() { return colourCode; }
    public boolean isOverBudget() { return variance > 0; }
}