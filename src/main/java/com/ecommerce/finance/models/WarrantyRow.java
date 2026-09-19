package com.ecommerce.finance.models;

public class WarrantyRow {
    private String addonType;
    private String productName;
    private double premiumAmount;

    public WarrantyRow(String addonType, String productName, double premiumAmount) {
        this.addonType = addonType;
        this.productName = productName;
        this.premiumAmount = premiumAmount;
    }

    public String getAddonType() { return addonType; }
    public String getProductName() { return productName; }
    public double getPremiumAmount() { return premiumAmount; }
}