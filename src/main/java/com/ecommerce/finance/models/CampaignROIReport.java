package com.ecommerce.finance.models;

import java.util.List;

public class CampaignROIReport {
    private int campaignId;
    private String campaignName;
    private double campaignCost;
    private double totalRevenue;
    private double totalDiscounts;
    private double roi;
    private String roiLabel;
    private List<CampaignOrder> orders;

    public CampaignROIReport(int campaignId, String campaignName, double campaignCost,
                             double totalRevenue, double totalDiscounts, double roi,
                             String roiLabel, List<CampaignOrder> orders) {
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.campaignCost = campaignCost;
        this.totalRevenue = totalRevenue;
        this.totalDiscounts = totalDiscounts;
        this.roi = roi;
        this.roiLabel = roiLabel;
        this.orders = orders;
    }

    // Getters
    public int getCampaignId() { return campaignId; }
    public String getCampaignName() { return campaignName; }
    public double getCampaignCost() { return campaignCost; }
    public double getTotalRevenue() { return totalRevenue; }
    public double getTotalDiscounts() { return totalDiscounts; }
    public double getRoi() { return roi; }
    public String getRoiLabel() { return roiLabel; }
    public List<CampaignOrder> getOrders() { return orders; }
}

