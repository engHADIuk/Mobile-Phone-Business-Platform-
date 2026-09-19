package com.ecommerce.finance.models;

public class Product {
    private int productId;
    private String name;
    private String category;
    private double price;
    private double cost;

    // Getters and Setters
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
}