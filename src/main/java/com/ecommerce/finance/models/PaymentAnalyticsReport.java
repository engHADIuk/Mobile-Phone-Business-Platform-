package com.ecommerce.finance.models;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class PaymentAnalyticsReport {
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<String, Double> methodRevenue;
    private Map<String, Integer> txCounts;
    private Map<String, Double> sharePercent;
    private List<String> flaggedMethods;

    public PaymentAnalyticsReport(LocalDate startDate, LocalDate endDate, Map<String, Double> methodRevenue,
                                  Map<String, Integer> txCounts, Map<String, Double> sharePercent,
                                  List<String> flaggedMethods) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.methodRevenue = methodRevenue;
        this.txCounts = txCounts;
        this.sharePercent = sharePercent;
        this.flaggedMethods = flaggedMethods;
    }

    // Getters
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Map<String, Double> getMethodRevenue() { return methodRevenue; }
    public Map<String, Integer> getTxCounts() { return txCounts; }
    public Map<String, Double> getSharePercent() { return sharePercent; }
    public List<String> getFlaggedMethods() { return flaggedMethods; }

    public boolean isEmpty() { return methodRevenue == null || methodRevenue.isEmpty(); }
    public double getMethodRevenue(String method) { return methodRevenue.getOrDefault(method, 0.0); }
    public boolean hasFlaggedMethods() { return flaggedMethods != null && !flaggedMethods.isEmpty(); }
}