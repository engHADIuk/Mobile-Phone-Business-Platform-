package com.ecommerce.finance.exceptions;

public class CampaignNotFoundException extends Exception {
    private int campaignId;

    public CampaignNotFoundException(String message, int campaignId) {
        super(message);
        this.campaignId = campaignId;
    }

    public int getCampaignId() { return campaignId; }
}