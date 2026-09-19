package com.nc1605.inventorywarehouse.model;

public class InventoryItem {
    private final int productId, warehouseId, quantity, threshold;
    private final String productName, warehouseName;
    public InventoryItem(int productId, String productName, int warehouseId, String warehouseName, int quantity, int threshold) {
        this.productId=productId; this.productName=productName; this.warehouseId=warehouseId; this.warehouseName=warehouseName; this.quantity=quantity; this.threshold=threshold;
    }
    public int getProductId(){ return productId; } public String getProductName(){ return productName; }
    public int getWarehouseId(){ return warehouseId; } public String getWarehouseName(){ return warehouseName; }
    public int getQuantity(){ return quantity; } public int getThreshold(){ return threshold; }
    public String getStatus(){ return quantity < threshold / 2.0 ? "Critical" : quantity < threshold ? "Low Stock" : "In Stock"; }
}
