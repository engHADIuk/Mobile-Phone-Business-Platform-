package com.ecommerce.finance.models;

import java.time.LocalDate;
import java.util.List;

public class Order {
    private int orderId;
    private String orderCode;
    private int customerId;
    private LocalDate orderDate;
    private String status;
    private double netRevenue;
    private double totalAmount;
    private List<OrderItem> items;
    private Customer customer;

    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getNetRevenue() { return netRevenue; }
    public void setNetRevenue(double netRevenue) { this.netRevenue = netRevenue; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public boolean isDelivered() {
        return "Delivered".equals(status);
    }
}