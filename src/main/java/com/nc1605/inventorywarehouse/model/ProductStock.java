package com.nc1605.inventorywarehouse.model;

/** A product and its total stock across every warehouse. */
public class ProductStock {
    private final int id;
    private final String name;
    private final int totalStock;

    public ProductStock(int id, String name, int totalStock) {
        this.id = id;
        this.name = name;
        this.totalStock = totalStock;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getTotalStock() { return totalStock; }
}
