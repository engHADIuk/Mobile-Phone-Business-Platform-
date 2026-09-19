package com.nc1605.inventorywarehouse.model;
public class Warehouse {
    private final int id, capacity, used; private final String name, location;
    public Warehouse(int id,String name,String location,int capacity,int used){this.id=id;this.name=name;this.location=location;this.capacity=capacity;this.used=used;}
    public int getId(){return id;} public String getName(){return name;} public String getLocation(){return location;}
    public int getCapacity(){return capacity;} public int getUsed(){return used;}
    public String getUtilisation(){return capacity == 0 ? "0%" : String.format("%.1f%%", used * 100.0 / capacity);}
    @Override public String toString(){return name + " (" + location + ")";}
}
