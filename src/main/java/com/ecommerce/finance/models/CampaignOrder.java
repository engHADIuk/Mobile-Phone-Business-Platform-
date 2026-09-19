package com.ecommerce.finance.models;

public class CampaignOrder {
    private int id;
    private int campaignId;
    private int orderId;
    private double discountApplied;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getCampaignId() { return campaignId; }
    public void setCampaignId(int campaignId) { this.campaignId = campaignId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public double getDiscountApplied() { return discountApplied; }
    public void setDiscountApplied(double discountApplied) { this.discountApplied = discountApplied; }
}