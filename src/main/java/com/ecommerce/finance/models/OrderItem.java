package com.ecommerce.finance.models;

public class OrderItem {
    private int itemId;
    private int orderId;
    private int productId;
    private int quantity;
    private double unitPrice;
    private double costPrice;
    private double lineTotal;
    private double lineCogs;

    // Getters and Setters
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public double getCostPrice() { return costPrice; }
    public void setCostPrice(double costPrice) { this.costPrice = costPrice; }
    public double getLineTotal() { return lineTotal; }
    public void setLineTotal(double lineTotal) { this.lineTotal = lineTotal; }
    public double getLineCogs() { return lineCogs; }
    public void setLineCogs(double lineCogs) { this.lineCogs = lineCogs; }

    public double getCOGS() {
        return quantity * costPrice;
    }
}